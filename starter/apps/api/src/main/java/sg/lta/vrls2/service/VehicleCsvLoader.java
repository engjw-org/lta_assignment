package sg.lta.vrls2.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvMalformedLineException;
import com.opencsv.exceptions.CsvValidationException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sg.lta.vrls2.model.Vehicle;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class VehicleCsvLoader {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleCsvLoader.class);
    private static final String[] HEADER = {
            "uuid", "status", "start_date", "end_date", "time", "user_uuid", "brand", "type"
    };
    // Brand/type limits are the application policy recorded in ADR 0001.
    private static final int[] MAX_LENGTHS = {36, 12, 10, 10, 8, 36, 100, 50};
    private static final Pattern UUID_FORMAT = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
            .ofPattern("dd/MM/uuuu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter
            .ofPattern("HH:mm:ss", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);

    public List<Vehicle> loadAll(Path dataDir) throws IOException, CsvException {
        List<Vehicle> result = new ArrayList<>();
        result.addAll(load(dataDir.resolve("personal.csv"), "personal", "REGISTERED"));
        result.addAll(load(dataDir.resolve("deregpersonal.csv"), "personal", "DEREGISTERED"));
        result.addAll(load(dataDir.resolve("commercial.csv"), "commercial", "REGISTERED"));
        result.addAll(load(dataDir.resolve("deregcommercial.csv"), "commercial", "DEREGISTERED"));
        return result;
    }

    private List<Vehicle> load(Path path, String category, String expectedStatus) throws IOException {
        List<Vehicle> rows = new ArrayList<>();
        long sourceRow = 1;
        try (Reader reader = Files.newBufferedReader(path);
             CSVReader csv = new CSVReaderBuilder(reader)
                     .withCSVParser(new RFC4180ParserBuilder().build()).build()) {
            if (!Arrays.equals(csv.readNext(), HEADER)) {
                reject(path, sourceRow, "schema", "Expected the eight declared columns in schema order");
                return rows;
            }
            while (true) {
                sourceRow = csv.getLinesRead() + 1;
                String[] row = csv.readNext();
                if (row == null) {
                    break;
                }
                Vehicle vehicle = validateRow(path, sourceRow, row, category, expectedStatus);
                if (vehicle != null) {
                    rows.add(vehicle);
                }
            }
        } catch (CsvValidationException | CsvMalformedLineException e) {
            // Broken CSV quoting makes subsequent record boundaries unreliable.
            reject(path, sourceRow, "schema", "Malformed CSV syntax; remaining file rejected");
        }
        return rows;
    }

    private Vehicle validateRow(Path path, long sourceRow, String[] row,
                                String category, String expectedStatus) {
        if (row.length != HEADER.length) {
            reject(path, sourceRow, "schema", "Expected exactly eight fields");
            return null;
        }
        for (int column = 0; column < HEADER.length; column++) {
            String value = row[column];
            if (value == null || value.isBlank()) {
                reject(path, sourceRow, HEADER[column], "Required field is missing or blank");
                return null;
            }
            if (value.length() > MAX_LENGTHS[column]) {
                reject(path, sourceRow, HEADER[column], "Exceeds maximum length of " + MAX_LENGTHS[column]);
                return null;
            }
            if (isWhitespace(value.codePointAt(0)) || isWhitespace(value.codePointBefore(value.length()))
                    || value.codePoints().anyMatch(Character::isISOControl)) {
                reject(path, sourceRow, HEADER[column], "Surrounding whitespace or control characters are not allowed");
                return null;
            }
        }
        for (int column : new int[]{0, 5}) {
            if (!UUID_FORMAT.matcher(row[column]).matches()) {
                reject(path, sourceRow, HEADER[column], "Expected a canonical 36-character UUID");
                return null;
            }
        }
        if (!expectedStatus.equals(row[1])) {
            reject(path, sourceRow, "status", "Expected " + expectedStatus + " for this source file");
            return null;
        }
        LocalDate[] dates = new LocalDate[2];
        for (int column = 2; column <= 3; column++) {
            try {
                dates[column - 2] = LocalDate.parse(row[column], DATE_FORMAT);
                if (dates[column - 2].getYear() < 1) {
                    reject(path, sourceRow, HEADER[column], "Year must be between 0001 and 9999");
                    return null;
                }
            } catch (DateTimeParseException e) {
                reject(path, sourceRow, HEADER[column], "Expected a real calendar date in DD/MM/YYYY format");
                return null;
            }
        }
        LocalTime time;
        try {
            time = LocalTime.parse(row[4], TIME_FORMAT);
        } catch (DateTimeParseException e) {
            reject(path, sourceRow, "time", "Expected a 24-hour time in HH:mm:ss format");
            return null;
        }
        return new Vehicle(row[0], row[1], dates[0], dates[1], time, row[5], row[6], row[7], category);
    }

    private static boolean isWhitespace(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint);
    }

    private void reject(Path path, long sourceRow, String field, String reason) {
        // JSON keeps diagnostics structured in the default console log without exposing raw field values.
        LOG.warn("{}", JsonNodeFactory.instance.objectNode()
                .put("event", "csv_row_rejected")
                .put("source", path.getFileName().toString())
                .put("row", sourceRow)
                .put("field", field)
                .put("reason", reason));
    }
}
