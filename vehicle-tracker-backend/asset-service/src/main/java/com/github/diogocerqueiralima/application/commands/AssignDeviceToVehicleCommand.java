package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command payload used by the presentation layer to request assigning a device to a vehicle.
 */
public record AssignDeviceToVehicleCommand(

        @NotNull(message = "deviceId is required")
        UUID deviceId,

        @NotNull(message = "vehicleId is required")
        UUID vehicleId,

        @NotNull(message = "assignedBy is required")
        UUID assignedBy,

        UUID installedBy,

        String notes

) {}

