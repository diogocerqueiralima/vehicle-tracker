package com.github.diogocerqueiralima.asset.service.domain.assignments

import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Base class for all assignments in the system.
 * An assignment can be a [VehicleAssignment] or a [SimCardAssignment].
 *
 * The [Assignment] is responsible for tracking the assignment history of assets,
 * including the timestamps of when the assignment was made and when it was removed,
 * as well as the users responsible for these actions.
 */
abstract class Assignment protected constructor(
    val id: Long?,
    val assignedAt: Instant,
    val unassignedAt: Instant?,
    val assignedBy: UUID,
    val unassignedBy: UUID?
) {

    protected constructor(
        assignedAt: Instant, unassignedAt: Instant?, assignedBy: UUID, unassignedBy: UUID?
    ) : this(null, assignedAt, unassignedAt, assignedBy, unassignedBy)

    val isActive: Boolean
        get() = unassignedAt == null

    /**
     * The duration of the assignment.
     * If the assignment is still active, it is the time from [assignedAt] to the current time.
     * If the assignment has been unassigned, it is the time from [assignedAt] to [unassignedAt].
     */
    val duration: Duration
        get() = Duration.between(assignedAt, unassignedAt ?: Instant.now())

}
