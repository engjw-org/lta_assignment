package sg.lta.vrls2.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import sg.lta.vrls2.model.Vehicle;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvSecurityBoundaryTest {
    @TempDir Path directory;

    @Test
    void rejectsLogInjectionWithoutEchoingRawValuesOrInternalPaths() throws Exception {
        CsvTestSupport.emptyFiles(directory);
        String[] invalid = CsvTestSupport.row(CsvTestSupport.BAD_UUID);
        invalid[6] = "issue7-forged\n{\"event\":\"accepted\"}";
        CsvTestSupport.write(directory.resolve("personal.csv"),
                List.of(invalid, CsvTestSupport.row(CsvTestSupport.FIRST_UUID)));
        Logger logger = (Logger) LoggerFactory.getLogger(VehicleCsvLoader.class);
        ListAppender<ILoggingEvent> captured = new ListAppender<>();
        captured.start();
        logger.addAppender(captured);
        try {
            assertThat(new VehicleCsvLoader().loadAll(directory)).extracting(Vehicle::uuid)
                    .containsExactly(CsvTestSupport.FIRST_UUID);
            assertThat(captured.list).hasSize(1);
            String message = captured.list.getFirst().getFormattedMessage();
            assertThat(message).doesNotContain("issue7-forged", directory.toString(), "\n", "\r");
            var diagnostic = new ObjectMapper().readTree(message);
            assertThat(diagnostic.path("event").asText()).isEqualTo("csv_row_rejected");
            assertThat(diagnostic.path("source").asText()).isEqualTo("personal.csv");
            assertThat(diagnostic.path("row").asInt()).isEqualTo(2);
            assertThat(diagnostic.path("field").asText()).isEqualTo("brand");
            assertThat(diagnostic.path("reason").asText()).isNotBlank();
        } finally {
            logger.detachAppender(captured);
            captured.stop();
        }
    }
}
