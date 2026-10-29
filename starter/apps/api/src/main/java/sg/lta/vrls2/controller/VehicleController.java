package sg.lta.vrls2.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import sg.lta.vrls2.model.Vehicle;
import sg.lta.vrls2.model.VehicleSearchCriteria;
import sg.lta.vrls2.model.VehicleSearchResult;
import sg.lta.vrls2.service.VehicleService;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "http://localhost:4200")
public class VehicleController {

    private static final int PAGE_SIZE = 50;

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("/search")
    public VehicleSearchResult search(@RequestBody VehicleSearchCriteria criteria) {
        return vehicleService.search(criteria);
    }

    @GetMapping
    public List<Vehicle> list(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(required = false) String uuid) {
        if (page < 0 || page > Integer.MAX_VALUE / PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page number");
        }
        List<Vehicle> all = vehicleService.all();
        if (uuid != null) {
            if (!uuid.matches("(?i)^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid vehicle UUID");
            }
            return all.stream().filter(vehicle -> vehicle.uuid().equals(uuid)).toList();
        }
        int start = page * PAGE_SIZE;
        if (start >= all.size()) {
            return List.of();
        }
        int end = start + PAGE_SIZE - 1;
        int safeEnd = Math.min(end, all.size());
        return all.subList(start, safeEnd);
    }

    @GetMapping("/{uuid}")
    public Vehicle one(@PathVariable String uuid) {
        return vehicleService.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
