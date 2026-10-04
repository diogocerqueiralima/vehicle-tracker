package com.github.diogocerqueiralima.asset.service.domain.assignments

import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard
import java.time.Instant
import java.util.UUID

/**
 * Represents the assignment of a SIM card to a device.
 * This class extends the base [Assignment] class and includes specific details related to SIM card assignments.
 */
class SimCardAssignment(
    id: Long?,
    val device: Device,
    val simCard: SimCard,
    assignedAt: Instant,
    unassignedAt: Instant?,
    assignedBy: UUID,
    unassignedBy: UUID?,
    val removalReason: SimCardRemovalReason?
) : Assignment(id, assignedAt, unassignedAt, assignedBy, unassignedBy) {

    constructor(
        device: Device, simCard: SimCard, assignedAt: Instant, unassignedAt: Instant?, assignedBy: UUID,
        unassignedBy: UUID?, removalReason: SimCardRemovalReason?
    ) : this(null, device, simCard, assignedAt, unassignedAt, assignedBy, unassignedBy, removalReason)

}
