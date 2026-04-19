package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.diogocerqueiralima.domain.assignments.VehicleRemovalReason;

import java.time.Instant;
import java.util.UUID;

/**
 * Vehicle assignment payload returned by presentation endpoints.
 */
public record VehicleAssignmentDTO(
        @JsonProperty("device_id") UUID deviceId,
        @JsonProperty("vehicle_id") UUID vehicleId,
        @JsonProperty("assigned_at") Instant assignedAt,
        @JsonProperty("assigned_by") UUID assignedBy,
        @JsonProperty("unassigned_at") Instant unassignedAt,
        @JsonProperty("unassigned_by") UUID unassignedBy,
        @JsonProperty("removal_reason") VehicleRemovalReason removalReason,
        @JsonProperty("installed_by") UUID installedBy,
        @JsonProperty("notes") String notes,
        @JsonProperty("active") boolean active
) {}

