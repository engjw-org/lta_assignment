package sg.lta.vrls2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sg.lta.vrls2.controller.VehicleController;
import sg.lta.vrls2.model.Vehicle;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Issue #7: real CSV -> service -> JSON; assertions compile before audit fields exist. */
class VehicleAuditTest {
    @TempDir Path directory;
    private final ObjectMapper json = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();

    @BeforeEach
    void prepareFiles() throws Exception {
        CsvTestSupport.emptyFiles(directory);
    }

    @ParameterizedTest
    @CsvSource({"personal.csv,REGISTERED", "deregpersonal.csv,DEREGISTERED",
            "commercial.csv,REGISTERED", "deregcommercial.csv,DEREGISTERED"})
    void everySourceRecordsItsFilenamePhysicalRowAndActualIngestionTime(String file, String status) throws Exception {
        String[] row = CsvTestSupport.row(CsvTestSupport.FIRST_UUID);
        row[1] = status;
        CsvTestSupport.write(directory.resolve(file), List.<String[]>of(row));
        Instant before = Instant.now();
        List<Vehicle> loaded = new VehicleCsvLoader().loadAll(directory);
        Instant after = Instant.now();
        assertThat(loaded).hasSize(1);
        assertAudit(json.valueToTree(loaded.getFirst()), file, 2, before, after);
    }

    @Test
    void identicalEventsAndCrossFileDuplicatesKeepDistinctSourceRows() throws Exception {
        String[] row = CsvTestSupport.row(CsvTestSupport.FIRST_UUID);
        CsvTestSupport.write(directory.resolve("personal.csv"), List.of(row, row));
        CsvTestSupport.write(directory.resolve("commercial.csv"), List.<String[]>of(row));
        Instant before = Instant.now();
        List<Vehicle> loaded = new VehicleCsvLoader().loadAll(directory);
        Instant after = Instant.now();
        assertThat(loaded).hasSize(3);
        assertAudit(json.valueToTree(loaded.get(0)), "personal.csv", 2, before, after);
        assertAudit(json.valueToTree(loaded.get(1)), "personal.csv", 3, before, after);
        assertAudit(json.valueToTree(loaded.get(2)), "commercial.csv", 2, before, after);
    }

    @Test
    void rejectedMultilineRowDoesNotRenumberTheNextAcceptedSourceRow() throws Exception {
        String[] invalid = CsvTestSupport.row(CsvTestSupport.BAD_UUID);
        invalid[6] = "untrusted\nbrand";
        CsvTestSupport.write(directory.resolve("personal.csv"), List.of(
                CsvTestSupport.row(CsvTestSupport.FIRST_UUID), invalid, CsvTestSupport.row(CsvTestSupport.LAST_UUID)));
        Instant before = Instant.now();
        List<Vehicle> loaded;
        try (CsvRejectionLogs logs = new CsvRejectionLogs()) {
            loaded = new VehicleCsvLoader().loadAll(directory);
            assertThat(loaded).extracting(Vehicle::uuid)
                    .containsExactly(CsvTestSupport.FIRST_UUID, CsvTestSupport.LAST_UUID);
            logs.assertRejected("personal.csv", 3, "brand");
        }
        assertAudit(json.valueToTree(loaded.getLast()), "personal.csv", 5, before, Instant.now());
    }

    @ParameterizedTest
    @ValueSource(strings = {"list", "uuid-history", "single", "search"})
    void servingPathsPreserveAuditCapturedDuringIngestion(String route) throws Exception {
        CsvTestSupport.write(directory.resolve("personal.csv"),
                List.<String[]>of(CsvTestSupport.row(CsvTestSupport.FIRST_UUID)));
        Instant before = Instant.now();
        VehicleService service = service();
        Instant afterLoad = Instant.now();
        MockMvc mvc = mvc(service);
        var request = switch (route) {
            case "list" -> get("/api/vehicles");
            case "uuid-history" -> get("/api/vehicles").param("uuid", CsvTestSupport.FIRST_UUID);
            case "single" -> get("/api/vehicles/" + CsvTestSupport.FIRST_UUID);
            default -> post("/api/vehicles/search").contentType(MediaType.APPLICATION_JSON).content("{}");
        };
        JsonNode first = json.readTree(mvc.perform(request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        JsonNode record = route.equals("search") ? first.path("records").get(0)
                : route.equals("single") ? first : first.get(0);
        assertAudit(record, "personal.csv", 2, before, afterLoad);
        // Compare wire values on both sides: an in-memory LongNode and parsed IntNode can represent the same row.
        JsonNode ingested = json.readTree(json.writeValueAsString(service.all().getFirst()));
        assertThat(record.path("audit")).isEqualTo(ingested.path("audit"));
        JsonNode repeated = json.readTree(mvc.perform(request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(repeated).as("serving does not regenerate the ingestion timestamp").isEqualTo(first);
    }

    @Test
    void sortingCappingAndUncappedHistoryKeepTheOriginalSourceReferences() throws Exception {
        List<String[]> rows = IntStream.rangeClosed(1, 2001).mapToObj(i -> {
            String[] row = CsvTestSupport.row(CsvTestSupport.FIRST_UUID);
            row[6] = "Event " + i;
            row[2] = LocalDate.of(2020, 1, 1).plusDays(i).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
            return row;
        }).toList();
        CsvTestSupport.write(directory.resolve("personal.csv"), rows);
        Instant before = Instant.now();
        VehicleService service = service();
        Instant afterLoad = Instant.now();
        MockMvc mvc = mvc(service);
        JsonNode search = json.readTree(mvc.perform(post("/api/vehicles/search")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(search.path("records").size()).isEqualTo(2000);
        assertThat(search.path("totalMatches").asInt()).isEqualTo(2001);
        assertThat(search.path("truncated").asBoolean()).isTrue();
        assertThat(search.path("records").get(0).path("brand").asText()).isEqualTo("Event 2001");
        assertThat(search.path("records").get(1999).path("brand").asText()).isEqualTo("Event 2");
        for (JsonNode record : search.path("records")) {
            int sourceRow = Integer.parseInt(record.path("brand").asText().substring(6)) + 1;
            assertAudit(record, "personal.csv", sourceRow, before, afterLoad);
        }
        JsonNode history = json.readTree(mvc.perform(get("/api/vehicles").param("uuid", CsvTestSupport.FIRST_UUID))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(history.size()).isEqualTo(2001);
        assertAudit(history.get(0), "personal.csv", 2, before, afterLoad); // Event omitted by the search cap.
        assertAudit(history.get(2000), "personal.csv", 2002, before, afterLoad);
    }

    private VehicleService service() throws Exception {
        VehicleService service = new VehicleService(new VehicleCsvLoader());
        ReflectionTestUtils.setField(service, "dataDir", directory.toString());
        service.init();
        return service;
    }

    private MockMvc mvc(VehicleService service) {
        return MockMvcBuilders.standaloneSetup(new VehicleController(service))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(json)).build();
    }

    private void assertAudit(JsonNode record, String source, int row, Instant before, Instant after) {
        JsonNode audit = record.path("audit");
        assertThat(audit.isObject()).as("R-6.4 accepted record must carry audit metadata").isTrue();
        List<String> fields = new ArrayList<>();
        audit.fieldNames().forEachRemaining(fields::add);
        assertThat(fields).containsExactlyInAnyOrder("sourceFile", "sourceRow", "ingestedAt");
        assertThat(audit.path("sourceFile").asText()).isEqualTo(source).doesNotContain(directory.toString());
        assertThat(audit.path("sourceRow").isIntegralNumber()).isTrue();
        assertThat(audit.path("sourceRow").asInt()).isEqualTo(row);
        assertThat(audit.path("ingestedAt").asText()).endsWith("Z");
        assertThat(Instant.parse(audit.path("ingestedAt").asText())).isBetween(before, after);
    }
}
