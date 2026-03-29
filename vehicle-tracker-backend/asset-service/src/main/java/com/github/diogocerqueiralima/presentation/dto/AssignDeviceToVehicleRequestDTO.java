package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO used to assign a device to a vehicle.
 */
public record AssignDeviceToVehicleRequestDTO(

        @NotNull(message = "deviceId is required")
        @JsonProperty("device_id")
        UUID deviceId,

        @NotNull(message = "vehicleId is required")
        @JsonProperty("vehicle_id")
        UUID vehicleId,

        @JsonProperty("installed_by")
        UUID installedBy,

        @JsonProperty("notes")
        String notes

) {}

