package sg.lta.vrls2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;

/** Validated search input; normalization here never changes the CSV events. */
public record VehicleSearchCriteria(String query, LocalDate startDate, LocalDate endDate, String type) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static VehicleSearchCriteria fromJson(JsonNode input) {
        if (input == null || !input.isObject()) {
            throw new IllegalArgumentException("Search criteria must be an object");
        }
        return new VehicleSearchCriteria(text(input, "query", 100), date(input, "startDate"),
                date(input, "endDate"), text(input, "type", 50));
    }

    private static String text(JsonNode input, String field, int maxLength) {
        JsonNode value = input.get(field);
        if (value == null || value.isNull()) {
            return "";
        }
        if (!value.isTextual() || value.textValue().length() > maxLength) {
            throw new IllegalArgumentException("Invalid " + field);
        }
        return value.textValue().strip();
    }

    private static LocalDate date(JsonNode input, String field) {
        String value = text(input, field, 10);
        if (value.isEmpty()) {
            return null;
        }
        if (!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new IllegalArgumentException("Invalid " + field);
        }
        LocalDate date = LocalDate.parse(value);
        if (date.getYear() < 1) {
            throw new IllegalArgumentException("Invalid " + field);
        }
        return date;
    }
}
