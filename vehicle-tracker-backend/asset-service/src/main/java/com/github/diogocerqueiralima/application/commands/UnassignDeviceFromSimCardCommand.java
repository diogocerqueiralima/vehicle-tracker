package com.github.diogocerqueiralima.application.commands;

import com.github.diogocerqueiralima.domain.assignments.SimCardRemovalReason;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command payload used by the presentation layer to request unassigning a device from a SIM card.
 */
public record UnassignDeviceFromSimCardCommand(

        @NotNull(message = "deviceId is required")
        UUID deviceId,

        @NotNull(message = "simCardId is required")
        UUID simCardId,

        @NotNull(message = "unassignedBy is required")
        UUID unassignedBy,

        SimCardRemovalReason removalReason

) {}

