package sg.lta.vrls2.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Observes real application logs. The proposed field contract is documented in ADR 0001. */
final class CsvRejectionLogs implements AutoCloseable {
    private final Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final ObjectMapper mapper = new ObjectMapper();

    CsvRejectionLogs() {
        appender.start();
        root.addAppender(appender);
    }

    List<JsonNode> records() {
        List<JsonNode> records = new ArrayList<>();
        for (ILoggingEvent event : appender.list) {
            ObjectNode fields = mapper.createObjectNode();
            try {
                JsonNode message = mapper.readTree(event.getFormattedMessage());
                if (message instanceof ObjectNode object) {
                    fields.setAll(object);
                }
            } catch (IOException | IllegalArgumentException ignored) {
                // Ordinary non-JSON messages can still carry structured SLF4J fields.
            }
            if (event.getKeyValuePairs() != null) {
                event.getKeyValuePairs().forEach(pair -> fields.set(pair.key, mapper.valueToTree(pair.value)));
            }
            if (fields.has("source") && fields.has("row") && fields.has("field") && fields.has("reason")) {
                records.add(fields);
            }
        }
        return records;
    }

    void assertRejected(String source, int row, String field) {
        assertThat(records()).as("R-6.1 structured rejection for %s:%s (%s)", source, row, field)
                .anySatisfy(record -> {
                    assertThat(record.path("source").asText()).isEqualTo(source);
                    assertThat(record.path("row").asInt()).isEqualTo(row);
                    assertThat(record.path("field").asText()).isEqualTo(field);
                    assertThat(record.path("reason").asText()).isNotBlank();
                });
    }

    @Override
    public void close() {
        root.detachAppender(appender);
        appender.stop();
    }
}
