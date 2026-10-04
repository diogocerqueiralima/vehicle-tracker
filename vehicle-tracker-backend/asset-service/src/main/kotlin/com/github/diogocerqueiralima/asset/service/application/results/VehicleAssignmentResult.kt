package com.github.diogocerqueiralima.asset.service.application.results

import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleRemovalReason
import java.time.Instant
import java.util.UUID

/**
 * Result returned by vehicle assignment application use cases.
 */
data class VehicleAssignmentResult(
    val deviceId: UUID,
    val vehicleId: UUID,
    val assignedAt: Instant,
    val assignedBy: UUID,
    val unassignedAt: Instant?,
    val unassignedBy: UUID?,
    val removalReason: VehicleRemovalReason?,
    val installedBy: UUID?,
    val notes: String?,
    val active: Boolean
)
