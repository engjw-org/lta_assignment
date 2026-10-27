package sg.lta.vrls2.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record Vehicle(
        String uuid,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime time,
        String userUuid,
        String brand,
        String type,
        String category
) {
}
