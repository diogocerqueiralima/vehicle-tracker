package com.github.diogocerqueiralima.asset.service.application.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 *
 * Command to retrieve a vehicle assignment by device id.
 *
 * @param deviceId the id of the device whose assignment is being queried.
 */
public record GetVehicleAssignmentByDeviceIdCommand(@NotNull UUID deviceId) {}
