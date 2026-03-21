package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.diogocerqueiralima.application.validation.Plate;
import com.github.diogocerqueiralima.application.validation.VIN;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

/**
 * Request DTO for vehicle creation.
 */
public record CreateVehicleRequestDTO(

        @NotBlank(message = "vin is required")
        @VIN
        @JsonProperty("vin")
        String vin,

        @NotBlank(message = "plate is required")
        @Plate
        @JsonProperty("plate")
        String plate,

        @NotBlank(message = "model is required")
        @JsonProperty("model")
        String model,

        @NotBlank(message = "manufacturer is required")
        @JsonProperty("manufacturer")
        String manufacturer,

        @NotNull(message = "manufacturingDate is required")
        @PastOrPresent(message = "manufacturingDate cannot be in the future")
        @JsonProperty("manufacturing_date")
        LocalDate manufacturingDate

) {}
