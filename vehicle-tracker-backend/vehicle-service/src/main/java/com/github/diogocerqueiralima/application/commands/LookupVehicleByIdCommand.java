package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 *
 * Command to look up a vehicle by its ID.
 * The entry point for the vehicle lookup use case.
 *
 * @param id the unique identifier of the vehicle
 */
public record LookupVehicleByIdCommand(@NotNull UUID id) {}
