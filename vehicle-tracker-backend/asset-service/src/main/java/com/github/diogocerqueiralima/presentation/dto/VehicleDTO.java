package com.github.diogocerqueiralima.presentation.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Vehicle payload returned by presentation endpoints.
 */
public record VehicleDTO(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String vin,
        String plate,
        String model,
        String manufacturer,
        LocalDate manufacturingDate
) {}

