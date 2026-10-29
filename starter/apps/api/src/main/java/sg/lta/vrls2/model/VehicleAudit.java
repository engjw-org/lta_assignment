package sg.lta.vrls2.model;

import java.time.Instant;

/** Source coordinates and ingestion time travel with the immutable event. */
public record VehicleAudit(String sourceFile, long sourceRow, Instant ingestedAt) {
}
