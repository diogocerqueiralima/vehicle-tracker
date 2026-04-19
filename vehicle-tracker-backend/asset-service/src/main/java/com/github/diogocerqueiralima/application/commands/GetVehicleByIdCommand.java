package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command payload used by the presentation layer to request a vehicle by id.
 */
public record GetVehicleByIdCommand(

        @NotNull(message = "id is required")
        UUID id,

        @NotNull(message = "userId is required")
        UUID userId

) {}

