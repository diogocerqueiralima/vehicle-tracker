package com.github.diogocerqueiralima.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Vehicle payload returned by presentation endpoints.
 */
@Schema(description = "Vehicle information returned by the API.")
public record VehicleDTO(

        @Schema(description = "Unique identifier of the vehicle.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty("id") UUID id,

        @Schema(description = "Timestamp when the vehicle was created.", example = "2024-01-15T10:30:00Z")
        @JsonProperty("created_at") Instant createdAt,

        @Schema(description = "Timestamp when the vehicle was last updated.", example = "2024-06-01T08:00:00Z")
        @JsonProperty("updated_at") Instant updatedAt,

        @Schema(description = "Vehicle Identification Number (VIN).", example = "1HGCM82633A123456")
        @JsonProperty("vin") String vin,

        @Schema(description = "License plate number of the vehicle.", example = "AB-12-CD")
        @JsonProperty("plate") String plate,

        @Schema(description = "Model name of the vehicle.", example = "Civic")
        @JsonProperty("model") String model,

        @Schema(description = "Manufacturer of the vehicle.", example = "Honda")
        @JsonProperty("manufacturer") String manufacturer,

        @Schema(description = "Date when the vehicle was manufactured.", example = "2020-03-15")
        @JsonProperty("manufacturing_date") LocalDate manufacturingDate

) {}
