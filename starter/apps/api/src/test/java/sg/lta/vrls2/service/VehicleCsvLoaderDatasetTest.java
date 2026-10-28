package sg.lta.vrls2.service;

import com.opencsv.CSVReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sg.lta.vrls2.model.Vehicle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static sg.lta.vrls2.service.CsvTestSupport.HEADER;

/** Exercises the real loader against every source row, keeping the supplied CSVs unchanged. */
class VehicleCsvLoaderDatasetTest {
    private static final Path DATA = Path.of("../../data");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    // Observed source defects, not a second implementation of the production validation rules.
    private static final Map<String, Set<Integer>> INVALID_TIME_LINES = Map.of(
            "personal.csv", Set.of(),
            "commercial.csv", Set.of(33),
            "deregpersonal.csv", Set.of(9, 11, 21),
            "deregcommercial.csv", Set.of(2, 3, 8, 41)
    );

    @ParameterizedTest(name = "all {1} source rows in {0}")
    @CsvSource({
            "personal.csv, 42, 42, REGISTERED, personal",
            "commercial.csv, 34, 33, REGISTERED, commercial",
            "deregpersonal.csv, 98, 95, DEREGISTERED, personal",
            "deregcommercial.csv, 60, 56, DEREGISTERED, commercial"
    })
    void returnsEveryValidEventWithCorrectFieldsAndLogsEachInvalidSourceRow(
            String file, int sourceCount, int acceptedCount, String status, String category) throws Exception {
        List<String[]> source;
        try (CSVReader reader = new CSVReader(Files.newBufferedReader(DATA.resolve(file)))) {
            source = reader.readAll();
        }
        assertThat(source.getFirst()).containsExactly(HEADER);
        assertThat(source).hasSize(sourceCount + 1);
        Set<Integer> rejectedLines = INVALID_TIME_LINES.get(file);
        List<Vehicle> expected = new ArrayList<>();
        for (int index = 1; index < source.size(); index++) {
            String[] row = source.get(index);
            if (rejectedLines.contains(index + 1)) {
                // Fail clearly if the source changes instead of silently weakening the regression.
                assertThat(row[4]).isIn("9:00:00", "9:30:00");
                continue;
            }
            expected.add(new Vehicle(row[0], row[1], LocalDate.parse(row[2], DATE),
                    LocalDate.parse(row[3], DATE), LocalTime.parse(row[4]), row[5], row[6], row[7], category));
        }
        assertThat(expected).hasSize(acceptedCount);

        try (CsvRejectionLogs logs = new CsvRejectionLogs()) {
            List<Vehicle> all = new VehicleCsvLoader().loadAll(DATA);
            List<Vehicle> actual = all.stream()
                    .filter(vehicle -> vehicle.status().equals(status) && vehicle.category().equals(category))
                    .toList();

            assertAll(
                    () -> assertThat(actual).as("every field of every accepted row in %s", file)
                            .containsExactlyInAnyOrderElementsOf(expected),
                    () -> {
                        for (int line : rejectedLines) {
                            logs.assertRejected(file, line, "time");
                        }
                        assertThat(logs.records().stream()
                                .filter(record -> record.path("source").asText().equals(file)).toList())
                                .as("one structured rejection per invalid source row").hasSize(rejectedLines.size());
                    }
            );
        }
    }
}
