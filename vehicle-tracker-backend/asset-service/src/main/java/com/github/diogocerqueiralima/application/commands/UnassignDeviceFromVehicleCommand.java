package com.github.diogocerqueiralima.application.commands;

import com.github.diogocerqueiralima.domain.assignments.VehicleRemovalReason;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command payload used by the presentation layer to request unassigning a device from a vehicle.
 */
public record UnassignDeviceFromVehicleCommand(

        @NotNull(message = "deviceId is required")
        UUID deviceId,

        @NotNull(message = "vehicleId is required")
        UUID vehicleId,

        @NotNull(message = "unassignedBy is required")
        UUID unassignedBy,

        VehicleRemovalReason removalReason

) {}

