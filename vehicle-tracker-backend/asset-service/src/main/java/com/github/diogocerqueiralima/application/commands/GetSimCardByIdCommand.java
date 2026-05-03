package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command payload used by the presentation layer to request SIM card retrieval by id.
 */
public record GetSimCardByIdCommand(

        @NotNull(message = "id is required")
        UUID id,

        @NotNull(message = "userId is required")
        UUID userId

) {}

