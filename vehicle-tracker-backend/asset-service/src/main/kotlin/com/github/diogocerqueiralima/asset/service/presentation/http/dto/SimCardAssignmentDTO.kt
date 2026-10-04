package com.github.diogocerqueiralima.asset.service.presentation.http.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardRemovalReason
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "SIM card assignment information returned by the API.")
data class SimCardAssignmentDTO(
    @field:Schema(description = "Unique identifier of the assigned device.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("device_id")
    val deviceId: UUID,
    @field:Schema(description = "Unique identifier of the SIM card.", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
    @JsonProperty("sim_card_id")
    val simCardId: UUID,
    @field:Schema(description = "Timestamp when the device was assigned to the SIM card.", example = "2024-03-10T14:00:00Z")
    @JsonProperty("assigned_at")
    val assignedAt: Instant,
    @field:Schema(description = "Identifier of the user who assigned the device.", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    @JsonProperty("assigned_by")
    val assignedBy: UUID,
    @field:Schema(description = "Timestamp when the device was unassigned from the SIM card. Null if still active.", example = "2024-06-01T09:00:00Z", nullable = true)
    @JsonProperty("unassigned_at")
    val unassignedAt: Instant?,
    @field:Schema(description = "Identifier of the user who unassigned the device. Null if still active.", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", nullable = true)
    @JsonProperty("unassigned_by")
    val unassignedBy: UUID?,
    @field:Schema(description = "Reason for removing the device from the SIM card. Null if still active.", example = "DAMAGE", nullable = true)
    @JsonProperty("removal_reason")
    val removalReason: SimCardRemovalReason?,
    @field:Schema(description = "Whether this assignment is currently active.", example = "true")
    @JsonProperty("active")
    val active: Boolean
)
