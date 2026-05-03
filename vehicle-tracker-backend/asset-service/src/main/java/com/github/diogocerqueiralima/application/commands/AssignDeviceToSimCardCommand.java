package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command payload used by the presentation layer to request assigning a device to a SIM card.
 */
public record AssignDeviceToSimCardCommand(

        @NotNull(message = "deviceId is required")
        UUID deviceId,

        @NotNull(message = "simCardId is required")
        UUID simCardId,

        @NotNull(message = "assignedBy is required")
        UUID assignedBy

) {}

