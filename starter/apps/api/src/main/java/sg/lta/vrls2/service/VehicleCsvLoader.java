package sg.lta.vrls2.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
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
import java.util.ArrayList;
import java.util.List;

@Component
public class VehicleCsvLoader {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public List<Vehicle> loadAll(Path dataDir) throws IOException, CsvException {
        List<Vehicle> result = new ArrayList<>();
        result.addAll(load(dataDir.resolve("personal.csv"), "personal"));
        result.addAll(load(dataDir.resolve("deregpersonal.csv"), "personal"));
        result.addAll(load(dataDir.resolve("commercial.csv"), "commercial"));
        result.addAll(load(dataDir.resolve("deregcommercial.csv"), "commercial"));
        return result;
    }

    private List<Vehicle> load(Path path, String category) throws IOException, CsvException {
        List<Vehicle> rows = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(path);
             CSVReader csv = new CSVReader(reader)) {
            List<String[]> all = csv.readAll();
            for (int i = 1; i < all.size(); i++) {
                String[] row = all.get(i);
                rows.add(new Vehicle(
                        row[0],
                        row[1],
                        parseDate(row[2]),
                        parseDate(row[3]),
                        parseTime(row[4]),
                        row[5],
                        row[6],
                        row[7],
                        category
                ));
            }
        }
        return rows;
    }

    LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    LocalTime parseTime(String raw) {
        try {
            return LocalTime.parse(raw, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
