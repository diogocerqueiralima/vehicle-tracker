package com.github.diogocerqueiralima.application.results;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Result returned by vehicle application use cases.
 */
public record VehicleResult(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String vin,
        String plate,
        String model,
        String manufacturer,
        LocalDate manufacturingDate
) {}

