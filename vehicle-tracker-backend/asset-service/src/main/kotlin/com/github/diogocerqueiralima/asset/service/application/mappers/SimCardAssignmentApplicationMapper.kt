package com.github.diogocerqueiralima.asset.service.application.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.results.SimCardAssignmentResult
import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard
import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardAssignment
import java.time.Instant

/**
 * Mapper for SIM card assignment conversions in the application layer.
 */
object SimCardAssignmentApplicationMapper {

    /**
     * Builds a domain SIM card assignment from the command payload and resolved assets.
     *
     * @param command assignment command payload.
     * @param device resolved device domain object.
     * @param simCard resolved SIM card domain object.
     * @param assignedAt timestamp when assignment is created.
     * @return domain SIM card assignment ready for persistence.
     */
    fun toDomain(
        command: AssignDeviceToSimCardCommand,
        device: Device,
        simCard: SimCard,
        assignedAt: Instant
    ): SimCardAssignment =
        SimCardAssignment(
            device,
            simCard,
            assignedAt,
            null,
            command.assignedBy,
            null,
            null
        )

    /**
     * Builds an application result from a domain SIM card assignment.
     *
     * @param simCardAssignment domain SIM card assignment.
     * @return assignment result contract.
     */
    fun toResult(simCardAssignment: SimCardAssignment): SimCardAssignmentResult =
        SimCardAssignmentResult(
            simCardAssignment.device.id,
            simCardAssignment.simCard.id,
            simCardAssignment.assignedAt,
            simCardAssignment.assignedBy,
            simCardAssignment.unassignedAt,
            simCardAssignment.unassignedBy,
            simCardAssignment.removalReason,
            simCardAssignment.isActive
        )

    /**
     * Builds an updated domain SIM card assignment with unassignment data.
     *
     * @param command unassignment command payload.
     * @param activeAssignment currently active assignment.
     * @param unassignedAt timestamp when assignment is being closed.
     * @return domain assignment ready to be persisted as inactive.
     */
    fun toDomain(
        command: UnassignDeviceFromSimCardCommand,
        activeAssignment: SimCardAssignment,
        unassignedAt: Instant
    ): SimCardAssignment =
        SimCardAssignment(
            activeAssignment.id,
            activeAssignment.device,
            activeAssignment.simCard,
            activeAssignment.assignedAt,
            unassignedAt,
            activeAssignment.assignedBy,
            command.unassignedBy,
            command.removalReason
        )

}
