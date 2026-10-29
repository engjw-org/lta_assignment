package sg.lta.vrls2.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sg.lta.vrls2.model.Vehicle;
import sg.lta.vrls2.model.VehicleSearchCriteria;
import sg.lta.vrls2.model.VehicleSearchResult;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class VehicleService {

    private static final int SEARCH_LIMIT = 2000;
    private static final Comparator<Vehicle> SEARCH_ORDER = Comparator.comparing(Vehicle::uuid)
            .thenComparing(Vehicle::startDate, Comparator.reverseOrder());

    private final VehicleCsvLoader loader;
    private List<Vehicle> vehicles = Collections.emptyList();

    @Value("${vrls.data-dir:../../data}")
    private String dataDir;

    public VehicleService(VehicleCsvLoader loader) {
        this.loader = loader;
    }

    @PostConstruct
    public void init() throws Exception {
        Path path = Paths.get(dataDir).toAbsolutePath().normalize();
        this.vehicles = loader.loadAll(path);
    }

    public List<Vehicle> all() {
        return vehicles;
    }

    public VehicleSearchResult search(VehicleSearchCriteria criteria) {
        String query = criteria.query().toLowerCase(Locale.ROOT);
        // Search the whole validated history and order matches before applying the response cap.
        List<Vehicle> matches = vehicles.stream()
                .filter(vehicle -> query.isEmpty()
                        || vehicle.uuid().toLowerCase(Locale.ROOT).startsWith(query)
                        || vehicle.brand().toLowerCase(Locale.ROOT).contains(query))
                .filter(vehicle -> criteria.type().isEmpty() || vehicle.type().equals(criteria.type()))
                .filter(vehicle -> criteria.startDate() == null || !vehicle.startDate().isBefore(criteria.startDate()))
                .filter(vehicle -> criteria.endDate() == null || !vehicle.endDate().isAfter(criteria.endDate()))
                .sorted(SEARCH_ORDER)
                .toList();
        return new VehicleSearchResult(List.copyOf(matches.subList(0, Math.min(SEARCH_LIMIT, matches.size()))),
                matches.size(), matches.size() > SEARCH_LIMIT);
    }

    public Optional<Vehicle> findByUuid(String uuid) {
        return vehicles.stream().filter(v -> v.uuid().equals(uuid)).findFirst();
    }
}
