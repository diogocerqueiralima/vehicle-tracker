package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.diogocerqueiralima.domain.assignments.VehicleRemovalReason;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO used to unassign a device from a vehicle.
 */
public record UnassignDeviceFromVehicleRequestDTO(

        @NotNull(message = "deviceId is required")
        @JsonProperty("device_id")
        UUID deviceId,

        @NotNull(message = "vehicleId is required")
        @JsonProperty("vehicle_id")
        UUID vehicleId,

        @JsonProperty("removal_reason")
        VehicleRemovalReason removalReason

) {}

