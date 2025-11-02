package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 *
 * Command to look up a vehicle by its associated device ID.
 *
 * @param deviceId the unique identifier of the device
 */
public record LookupVehicleByDeviceIdCommand(@NotNull UUID deviceId) {}
