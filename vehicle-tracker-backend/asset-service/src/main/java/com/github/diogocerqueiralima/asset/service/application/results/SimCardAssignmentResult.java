package com.github.diogocerqueiralima.asset.service.application.results;

import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardRemovalReason;

import java.time.Instant;
import java.util.UUID;

/**
 * Result returned by SIM card assignment application use cases.
 */
public record SimCardAssignmentResult(
        UUID deviceId,
        UUID simCardId,
        Instant assignedAt,
        UUID assignedBy,
        Instant unassignedAt,
        UUID unassignedBy,
        SimCardRemovalReason removalReason,
        boolean active
) {}

