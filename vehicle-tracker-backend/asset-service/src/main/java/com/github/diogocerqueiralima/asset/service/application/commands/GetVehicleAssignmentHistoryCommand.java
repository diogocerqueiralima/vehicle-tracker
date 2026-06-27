package com.github.diogocerqueiralima.asset.service.application.commands;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 *
 * Command payload used by the presentation layer to request the assignment history of a vehicle.
 *
 * @param vehicleId The unique identifier of the vehicle for which the assignment history is being requested.
 * @param userId The unique identifier of the user making the request, used for authorization and auditing purposes.
 * @param pageNumber The pageNumber number of the assignment history results to retrieve, used for pagination of results.
 * @param pageSize The number of assignment history results to retrieve per page, used for pagination of results.
 */
public record GetVehicleAssignmentHistoryCommand(

        @NotNull(message = "vehicleId is required")
        UUID vehicleId,

        @NotNull(message = "userId is required")
        UUID userId,

        @Min(value = 1, message = "pageNumber must be greater than or equal to 1")
        int pageNumber,

        @Min(value = 1, message = "pageSize must be greater than zero")
        @Max(value = 50, message = "pageSize must be less than or equal to 50")
        int pageSize

) {}