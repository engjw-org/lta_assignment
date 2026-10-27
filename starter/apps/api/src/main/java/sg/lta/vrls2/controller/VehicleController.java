package sg.lta.vrls2.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import sg.lta.vrls2.model.Vehicle;
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

    @GetMapping
    public List<Vehicle> list(@RequestParam(defaultValue = "0") int page) {
        List<Vehicle> all = vehicleService.all();
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
