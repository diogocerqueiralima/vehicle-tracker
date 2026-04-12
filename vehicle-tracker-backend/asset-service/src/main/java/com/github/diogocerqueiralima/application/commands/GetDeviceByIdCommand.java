package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command payload used by the presentation layer to request a device by id.
 */
public record GetDeviceByIdCommand(

        @NotNull(message = "id is required")
        UUID id,

        @NotNull(message = "userId is required")
        UUID userId

) {}

