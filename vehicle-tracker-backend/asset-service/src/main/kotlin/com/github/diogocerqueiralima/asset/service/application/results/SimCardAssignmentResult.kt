package com.github.diogocerqueiralima.asset.service.application.results

import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardRemovalReason
import java.time.Instant
import java.util.UUID

/**
 * Result returned by SIM card assignment application use cases.
 */
data class SimCardAssignmentResult(
    val deviceId: UUID,
    val simCardId: UUID,
    val assignedAt: Instant,
    val assignedBy: UUID,
    val unassignedAt: Instant?,
    val unassignedBy: UUID?,
    val removalReason: SimCardRemovalReason?,
    val active: Boolean
)
