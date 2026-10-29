package sg.lta.vrls2.model;

import java.util.List;

public record VehicleSearchResult(List<Vehicle> records, int totalMatches, boolean truncated) {
}
