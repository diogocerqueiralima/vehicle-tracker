package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Request DTO used to assign a device to a vehicle.
 */
public record AssignDeviceToVehicleRequestDTO(

        @JsonProperty("device_id")
        UUID deviceId,

        @JsonProperty("vehicle_id")
        UUID vehicleId,

        @JsonProperty("installed_by")
        UUID installedBy,

        @JsonProperty("notes")
        String notes

) {}

