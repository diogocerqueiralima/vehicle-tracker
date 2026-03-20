package com.github.diogocerqueiralima.presentation.dto;

import com.github.diogocerqueiralima.application.validation.Plate;
import com.github.diogocerqueiralima.application.validation.VIN;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

/**
 * Request DTO for vehicle update.
 */
public record UpdateVehicleRequestDTO(

        @NotBlank(message = "vin is required")
        @VIN
        String vin,

        @NotBlank(message = "plate is required")
        @Plate
        String plate,

        @NotBlank(message = "model is required")
        String model,

        @NotBlank(message = "manufacturer is required")
        String manufacturer,

        @NotNull(message = "manufacturingDate is required")
        @PastOrPresent(message = "manufacturingDate cannot be in the future")
        LocalDate manufacturingDate

) {}

