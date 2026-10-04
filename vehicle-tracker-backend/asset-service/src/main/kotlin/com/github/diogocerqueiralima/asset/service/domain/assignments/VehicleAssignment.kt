package com.github.diogocerqueiralima.asset.service.domain.assignments

import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle
import java.time.Instant
import java.util.UUID

/**
 * Represents the assignment of a device to a vehicle.
 * This class extends the base [Assignment] class and includes specific details related to vehicle assignments.
 */
class VehicleAssignment(
    id: Long?,
    val device: Device,
    val vehicle: Vehicle,
    assignedAt: Instant,
    unassignedAt: Instant?,
    assignedBy: UUID,
    unassignedBy: UUID?,
    val removalReason: VehicleRemovalReason?,
    val installedBy: UUID?,
    val notes: String?
) : Assignment(id, assignedAt, unassignedAt, assignedBy, unassignedBy) {

    constructor(
        device: Device, vehicle: Vehicle, assignedAt: Instant, unassignedAt: Instant?, assignedBy: UUID,
        unassignedBy: UUID?, removalReason: VehicleRemovalReason?, installedBy: UUID?, notes: String?
    ) : this(
        null, device, vehicle, assignedAt, unassignedAt, assignedBy, unassignedBy, removalReason, installedBy, notes
    )

}
