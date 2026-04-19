package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Vehicle payload returned by presentation endpoints.
 */
public record VehicleDTO(
        @JsonProperty("id") UUID id,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("vin") String vin,
        @JsonProperty("plate") String plate,
        @JsonProperty("model") String model,
        @JsonProperty("manufacturer") String manufacturer,
        @JsonProperty("manufacturing_date") LocalDate manufacturingDate
) {}

