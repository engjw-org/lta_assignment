package sg.lta.vrls2.service;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class CsvTestSupport {
    static final String[] HEADER = {
            "uuid", "status", "start_date", "end_date", "time", "user_uuid", "brand", "type"
    };
    static final List<String> FILES = List.of(
            "personal.csv", "deregpersonal.csv", "commercial.csv", "deregcommercial.csv"
    );
    static final String FIRST_UUID = "00000000-0000-4000-8000-000000000001";
    static final String BAD_UUID = "00000000-0000-4000-8000-000000000002";
    static final String LAST_UUID = "00000000-0000-4000-8000-000000000003";

    private CsvTestSupport() {
    }

    static String[] row(String uuid) {
        // An unambiguous date isolates validation failures from the known date-order bug.
        return new String[]{uuid, "REGISTERED", "11/11/2022", "01/01/2032", "12:30:00",
                "11111111-1111-4111-8111-111111111111", "Toyota", "5-seat"};
    }

    static void emptyFiles(Path directory) throws IOException {
        for (String file : FILES) {
            write(directory.resolve(file), List.of());
        }
    }

    static void write(Path path, List<String[]> rows) throws IOException {
        write(path, HEADER, rows);
    }

    static void write(Path path, String[] header, List<String[]> rows) throws IOException {
        try (CSVWriter writer = new CSVWriter(Files.newBufferedWriter(path))) {
            writer.writeNext(header);
            writer.writeAll(rows);
        }
    }
}
