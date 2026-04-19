package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command payload used by the presentation layer to request a page of vehicles.
 */
public record GetVehiclePageCommand(

        @Min(value = 1, message = "pageNumber must be greater than or equal to 1")
        int pageNumber,

        @Min(value = 1, message = "pageSize must be greater than zero")
        @Max(value = 50, message = "pageSize must be less than or equal to 50")
        int pageSize,

        @NotNull(message = "userId is required")
        UUID userId

) {}

