package com.github.diogocerqueiralima.application.commands;

import com.github.diogocerqueiralima.application.validators.Plate;
import com.github.diogocerqueiralima.application.validators.Year;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateVehicleCommand(
        @NotBlank String vin, @NotBlank @Plate String plate, @NotBlank String model,
        @NotBlank String manufacturer, @NotNull @Year Integer year, @NotNull UUID ownerId
        ) {}
