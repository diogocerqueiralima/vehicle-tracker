package com.github.diogocerqueiralima.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.diogocerqueiralima.domain.assignments.VehicleRemovalReason;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Request DTO used to unassign a device from a vehicle.
 */
@Schema(description = "Request payload for unassigning a device from a vehicle.")
public record UnassignDeviceFromVehicleRequestDTO(

        @Schema(description = "Unique identifier of the device to unassign.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty("device_id")
        UUID deviceId,

        @Schema(description = "Reason for removing the device from the vehicle. Allowed values: UPGRADE, LOSS, SOLD, RETIRED, OTHER.", example = "UPGRADE")
        @JsonProperty("removal_reason")
        VehicleRemovalReason removalReason

) {}
