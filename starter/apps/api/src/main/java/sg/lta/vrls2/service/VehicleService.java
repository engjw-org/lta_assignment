package sg.lta.vrls2.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sg.lta.vrls2.model.Vehicle;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

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

    public Optional<Vehicle> findByUuid(String uuid) {
        return vehicles.stream().filter(v -> v.uuid().equals(uuid)).findFirst();
    }
}
