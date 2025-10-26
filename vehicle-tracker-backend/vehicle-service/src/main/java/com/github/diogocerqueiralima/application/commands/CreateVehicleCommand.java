package com.github.diogocerqueiralima.application.commands;

import com.github.diogocerqueiralima.application.validators.Plate;
import com.github.diogocerqueiralima.application.validators.Year;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 *
 * Command to create a new vehicle.
 * The entry point for the vehicle creation use case.
 *
 * @param vin the Vehicle Identification Number
 * @param plate the vehicle's license plate
 * @param model the vehicle model
 * @param manufacturer the vehicle manufacturer
 * @param year the manufacturing year
 * @param ownerId the ID of the vehicle owner
 */
public record CreateVehicleCommand(
        @NotBlank String vin, @NotBlank @Plate String plate, @NotBlank String model,
        @NotBlank String manufacturer, @NotNull @Year Integer year, @NotNull UUID ownerId
) {}