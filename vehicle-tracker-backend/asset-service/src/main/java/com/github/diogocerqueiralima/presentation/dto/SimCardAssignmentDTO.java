package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.diogocerqueiralima.domain.assignments.SimCardRemovalReason;

import java.time.Instant;
import java.util.UUID;

/**
 * SIM card assignment payload returned by presentation endpoints.
 */
public record SimCardAssignmentDTO(
        @JsonProperty("device_id") UUID deviceId,
        @JsonProperty("sim_card_id") UUID simCardId,
        @JsonProperty("assigned_at") Instant assignedAt,
        @JsonProperty("assigned_by") UUID assignedBy,
        @JsonProperty("unassigned_at") Instant unassignedAt,
        @JsonProperty("unassigned_by") UUID unassignedBy,
        @JsonProperty("removal_reason") SimCardRemovalReason removalReason,
        @JsonProperty("active") boolean active
) {}

