package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.diogocerqueiralima.domain.assignments.VehicleRemovalReason;

import java.util.UUID;

/**
 * Request DTO used to unassign a device from a vehicle.
 */
public record UnassignDeviceFromVehicleRequestDTO(

        @JsonProperty("device_id")
        UUID deviceId,

        @JsonProperty("vehicle_id")
        UUID vehicleId,

        @JsonProperty("removal_reason")
        VehicleRemovalReason removalReason

) {}

