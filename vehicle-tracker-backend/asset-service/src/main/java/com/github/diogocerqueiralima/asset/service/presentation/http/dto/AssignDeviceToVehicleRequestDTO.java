package com.github.diogocerqueiralima.asset.service.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Request DTO used to assign a device to a vehicle.
 */
@Schema(description = "Request payload for assigning a device to a vehicle.")
public record AssignDeviceToVehicleRequestDTO(

        @Schema(description = "Unique identifier of the device to assign.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty("device_id")
        UUID deviceId,

        @Schema(description = "Identifier of the technician who physically installed the device.", example = "b2c3d4e5-f6a7-8901-bcde-f12345678901", nullable = true)
        @JsonProperty("installed_by")
        UUID installedBy,

        @Schema(description = "Optional notes about the installation.", example = "Installed on front dashboard.", nullable = true)
        @JsonProperty("notes")
        String notes

) {}
