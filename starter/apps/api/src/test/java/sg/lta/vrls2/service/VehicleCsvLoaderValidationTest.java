package sg.lta.vrls2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.lta.vrls2.model.Vehicle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static sg.lta.vrls2.service.CsvTestSupport.*;

/** Issue #1: observable ingestion behaviour, without supplying a replacement validator. */
class VehicleCsvLoaderValidationTest {
    @TempDir
    Path directory;

    private final VehicleCsvLoader loader = new VehicleCsvLoader();

    @BeforeEach
    void prepareFiles() throws Exception {
        emptyFiles(directory);
    }

    @ParameterizedTest(name = "DD/MM/YYYY {0} must mean {1}")
    @CsvSource({
            "01/11/2022, 2022-11-01",
            "14/11/2022, 2022-11-14",
            "08/04/2026, 2026-04-08",
            "29/02/2024, 2024-02-29",
            "31/12/2026, 2026-12-31",
            "01/01/2026, 2026-01-01"
    })
    void parsesBothDateColumnsAsStrictDayMonthYear(String input, String expected) throws Exception {
        String[] row = row(FIRST_UUID);
        row[2] = input;
        row[3] = input;
        write(directory.resolve("personal.csv"), List.<String[]>of(row));

        List<Vehicle> vehicles = loader.loadAll(directory);

        assertThat(vehicles).singleElement().satisfies(vehicle -> {
            assertThat(vehicle.startDate()).isEqualTo(LocalDate.parse(expected));
            assertThat(vehicle.endDate()).isEqualTo(LocalDate.parse(expected));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"00:00:00", "09:00:00", "09:30:00", "23:59:59"})
    void acceptsValidTwentyFourHourTimes(String time) throws Exception {
        String[] row = row(FIRST_UUID);
        row[4] = time;
        write(directory.resolve("personal.csv"), List.<String[]>of(row));

        assertThat(loader.loadAll(directory)).singleElement()
                .satisfies(vehicle -> assertThat(vehicle.time()).isEqualTo(LocalTime.parse(time)));
    }

    static Stream<Arguments> invalidFields() {
        List<Arguments> cases = new ArrayList<>();
        for (int column = 0; column < HEADER.length; column++) {
            for (String blank : List.of("", " ", "\t")) {
                cases.add(Arguments.of(HEADER[column] + " missing/blank", column, blank));
            }
        }
        for (int column : List.of(0, 5)) {
            for (String invalid : List.of("not-a-uuid", FIRST_UUID.replace("-", ""),
                    "g0000000-0000-4000-8000-000000000001", FIRST_UUID + "0")) {
                cases.add(Arguments.of(HEADER[column] + " invalid UUID", column, invalid));
            }
        }
        for (String status : List.of("UNKNOWN", "registered", "DEREGISTERED", "REGISTERED ")) {
            cases.add(Arguments.of("status invalid or contradicts file", 1, status));
        }
        for (int column : List.of(2, 3)) {
            for (String date : List.of("31/04/2026", "29/02/2023", "00/11/2022", "01/13/2022",
                    "2026-04-08", "8/04/2026", "08/4/2026", "08/04/26", "01/01/0000")) {
                cases.add(Arguments.of(HEADER[column] + " invalid date " + date, column, date));
            }
        }
        for (String time : List.of("9:00:00", "9:30:00", "24:00:00", "12:60:00", "12:30:60",
                "12:30", "12:30:00Z", "12:30:00.000")) {
            cases.add(Arguments.of("time invalid " + time, 4, time));
        }
        cases.add(Arguments.of("brand exceeds proposed 100-character limit", 6, "B".repeat(101)));
        cases.add(Arguments.of("type exceeds proposed 50-character limit", 7, "T".repeat(51)));
        for (int column : List.of(0, 2, 3, 4, 5, 6, 7)) {
            cases.add(Arguments.of(HEADER[column] + " must not be silently trimmed", column,
                    " " + row(BAD_UUID)[column]));
        }
        cases.add(Arguments.of("brand contains a log-forging newline", 6, "Ford\nforged log"));
        cases.add(Arguments.of("type contains a control character", 7, "5-seat\u0000"));
        return cases.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidFields")
    void rejectsInvalidFieldsLogsTheSourceAndContinues(String scenario, int column, String value)
            throws Exception {
        String[] invalid = row(BAD_UUID);
        invalid[column] = value;
        write(directory.resolve("personal.csv"), List.of(row(FIRST_UUID), invalid, row(LAST_UUID)));
        byte[] sourceBefore = Files.readAllBytes(directory.resolve("personal.csv"));

        try (CsvRejectionLogs logs = new CsvRejectionLogs()) {
            List<Vehicle> vehicles = assertDoesNotThrow(() -> loader.loadAll(directory),
                    "R-5.3: a malformed record must not abort ingestion");

            assertAll(
                    () -> assertThat(vehicles).extracting(Vehicle::uuid)
                            .as("R-6.1: only the two valid neighbouring records may reach the client")
                            .containsExactly(FIRST_UUID, LAST_UUID),
                    () -> logs.assertRejected("personal.csv", 3, HEADER[column]),
                    () -> assertThat(Files.readAllBytes(directory.resolve("personal.csv")))
                            .as("R-4.1: the source CSV must remain unchanged").isEqualTo(sourceBefore)
            );
        }
    }

    @ParameterizedTest
    @CsvSource({"1,1", "100,50"})
    void acceptsTextAtTheProposedLengthBoundaries(int brandLength, int typeLength) throws Exception {
        String[] row = row(FIRST_UUID);
        row[6] = "B".repeat(brandLength);
        row[7] = "T".repeat(typeLength);
        write(directory.resolve("personal.csv"), List.<String[]>of(row));

        assertThat(loader.loadAll(directory)).singleElement().satisfies(vehicle -> {
            assertThat(vehicle.brand()).isEqualTo(row[6]);
            assertThat(vehicle.type()).isEqualTo(row[7]);
        });
    }

    @Test
    void acceptsQuotedCommasUnicodeAndPreviouslyUnseenBrandsAndTypes() throws Exception {
        String[] row = row(FIRST_UUID);
        row[6] = "New Brand, 新";
        row[7] = "10-seat";
        write(directory.resolve("personal.csv"), List.<String[]>of(row));

        assertThat(loader.loadAll(directory)).singleElement().satisfies(vehicle -> {
            assertThat(vehicle.brand()).isEqualTo(row[6]);
            assertThat(vehicle.type()).isEqualTo(row[7]);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 7, 9})
    void rejectsRowsWithTheWrongNumberOfFieldsWithoutLosingTheirNeighbours(int columns) throws Exception {
        String[] malformed = Arrays.copyOf(row(BAD_UUID), columns);
        if (columns > HEADER.length) {
            malformed[8] = "unexpected";
        }
        write(directory.resolve("personal.csv"), List.of(row(FIRST_UUID), malformed, row(LAST_UUID)));

        try (CsvRejectionLogs logs = new CsvRejectionLogs()) {
            List<Vehicle> vehicles = assertDoesNotThrow(() -> loader.loadAll(directory));
            assertAll(
                    () -> assertThat(vehicles).extracting(Vehicle::uuid).containsExactly(FIRST_UUID, LAST_UUID),
                    () -> logs.assertRejected("personal.csv", 3, "schema")
            );
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"renamed", "reordered", "missing", "extra"})
    void rejectsAnInvalidHeaderAndContinuesWithOtherFiles(String problem) throws Exception {
        String[] header = HEADER.clone();
        switch (problem) {
            case "renamed" -> header[0] = "vehicle_id";
            case "reordered" -> {
                header[2] = "end_date";
                header[3] = "start_date";
            }
            case "missing" -> header = Arrays.copyOf(header, 7);
            case "extra" -> {
                header = Arrays.copyOf(header, 9);
                header[8] = "extra";
            }
            default -> throw new IllegalArgumentException(problem);
        }
        write(directory.resolve("personal.csv"), header, List.<String[]>of(row(BAD_UUID)));
        write(directory.resolve("commercial.csv"), List.<String[]>of(row(FIRST_UUID)));

        try (CsvRejectionLogs logs = new CsvRejectionLogs()) {
            List<Vehicle> vehicles = assertDoesNotThrow(() -> loader.loadAll(directory));
            assertAll(
                    () -> assertThat(vehicles).extracting(Vehicle::uuid).containsExactly(FIRST_UUID),
                    () -> logs.assertRejected("personal.csv", 1, "schema")
            );
        }
    }

    @ParameterizedTest
    @CsvSource({
            "personal.csv, REGISTERED, personal",
            "deregpersonal.csv, DEREGISTERED, personal",
            "commercial.csv, REGISTERED, commercial",
            "deregcommercial.csv, DEREGISTERED, commercial"
    })
    void validatesEveryFileAndPreservesItsCategory(String file, String status, String category) throws Exception {
        String[] valid = row(FIRST_UUID);
        valid[1] = status;
        String[] invalid = row(BAD_UUID);
        invalid[1] = status;
        invalid[4] = "9:00:00";
        write(directory.resolve(file), List.of(valid, invalid));

        try (CsvRejectionLogs logs = new CsvRejectionLogs()) {
            List<Vehicle> vehicles = assertDoesNotThrow(() -> loader.loadAll(directory));
            assertAll(
                    () -> assertThat(vehicles).singleElement().satisfies(vehicle -> {
                        assertThat(vehicle.uuid()).isEqualTo(FIRST_UUID);
                        assertThat(vehicle.status()).isEqualTo(status);
                        assertThat(vehicle.category()).isEqualTo(category);
                    }),
                    () -> logs.assertRejected(file, 3, "time")
            );
        }
    }

    @Test
    void preservesMultipleValidEventsForOneVehicle() throws Exception {
        String[] registered = row(FIRST_UUID);
        String[] laterRegistration = row(FIRST_UUID);
        laterRegistration[4] = "13:00:00";
        String[] deregistered = row(FIRST_UUID);
        deregistered[1] = "DEREGISTERED";
        deregistered[4] = "14:00:00";
        write(directory.resolve("personal.csv"), List.of(registered, laterRegistration));
        write(directory.resolve("deregpersonal.csv"), List.<String[]>of(deregistered));

        List<Vehicle> vehicles = loader.loadAll(directory);

        assertThat(vehicles).hasSize(3).allSatisfy(vehicle -> assertThat(vehicle.uuid()).isEqualTo(FIRST_UUID));
        assertThat(vehicles).extracting(Vehicle::time)
                .containsExactlyInAnyOrder(LocalTime.of(12, 30), LocalTime.of(13, 0), LocalTime.of(14, 0));
    }
}
