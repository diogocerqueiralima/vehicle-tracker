package com.github.diogocerqueiralima.application.results;

import com.github.diogocerqueiralima.domain.assignments.VehicleRemovalReason;

import java.time.Instant;
import java.util.UUID;

/**
 * Result returned by vehicle assignment application use cases.
 */
public record VehicleAssignmentResult(
        UUID deviceId,
        UUID vehicleId,
        Instant assignedAt,
        UUID assignedBy,
        Instant unassignedAt,
        UUID unassignedBy,
        VehicleRemovalReason removalReason,
        UUID installedBy,
        String notes,
        boolean active
) {}

