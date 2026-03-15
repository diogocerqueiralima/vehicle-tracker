package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

/**
 * Command payload used by the presentation layer to request vehicle creation.
 */
public record CreateVehicleCommand(

        @NotBlank(message = "vin is required")
        String vin,

        @NotBlank(message = "plate is required")
        String plate,

        @NotBlank(message = "model is required")
        String model,

        @NotBlank(message = "manufacturer is required")
        String manufacturer,

        @NotNull(message = "manufacturingDate is required")
        @PastOrPresent(message = "manufacturingDate cannot be in the future")
        LocalDate manufacturingDate

) {}

