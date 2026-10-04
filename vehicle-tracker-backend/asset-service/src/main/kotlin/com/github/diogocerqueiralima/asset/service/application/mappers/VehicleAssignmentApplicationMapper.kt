package com.github.diogocerqueiralima.asset.service.application.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult
import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleAssignment
import org.springframework.data.domain.Page
import java.time.Instant

/**
 * Mapper for vehicle assignment conversions in the application layer.
 */
object VehicleAssignmentApplicationMapper {

    /**
     * Builds a domain vehicle assignment from the command payload and resolved assets.
     *
     * @param command assignment command payload.
     * @param device resolved device domain object.
     * @param vehicle resolved vehicle domain object.
     * @param assignedAt timestamp when assignment is created.
     * @return domain vehicle assignment ready for persistence.
     */
    fun toDomain(
        command: AssignDeviceToVehicleCommand,
        device: Device,
        vehicle: Vehicle,
        assignedAt: Instant
    ): VehicleAssignment =
        VehicleAssignment(
            device,
            vehicle,
            assignedAt,
            null,
            command.assignedBy,
            null,
            null,
            command.installedBy,
            command.notes
        )

    /**
     * Builds an application result from a domain vehicle assignment.
     *
     * @param vehicleAssignment domain vehicle assignment.
     * @return assignment result contract.
     */
    fun toResult(vehicleAssignment: VehicleAssignment): VehicleAssignmentResult =
        VehicleAssignmentResult(
            vehicleAssignment.device.id,
            vehicleAssignment.vehicle.id,
            vehicleAssignment.assignedAt,
            vehicleAssignment.assignedBy,
            vehicleAssignment.unassignedAt,
            vehicleAssignment.unassignedBy,
            vehicleAssignment.removalReason,
            vehicleAssignment.installedBy,
            vehicleAssignment.notes,
            vehicleAssignment.isActive
        )

    /**
     * Builds an updated domain vehicle assignment with unassignment data.
     *
     * @param command unassignment command payload.
     * @param activeAssignment currently active assignment.
     * @param unassignedAt timestamp when assignment is being closed.
     * @return domain assignment ready to be persisted as inactive.
     */
    fun toDomain(
        command: UnassignDeviceFromVehicleCommand,
        activeAssignment: VehicleAssignment,
        unassignedAt: Instant
    ): VehicleAssignment =
        VehicleAssignment(
            activeAssignment.id,
            activeAssignment.device,
            activeAssignment.vehicle,
            activeAssignment.assignedAt,
            unassignedAt,
            activeAssignment.assignedBy,
            command.unassignedBy,
            command.removalReason,
            activeAssignment.installedBy,
            activeAssignment.notes
        )

    /**
     * Converts a paginated list of domain vehicle assignments into an application result contract.
     *
     * @param page paginated list of domain vehicle assignments.
     * @return paginated result contract with the list of vehicle assignment results and pagination metadata.
     */
    fun toResult(page: Page<VehicleAssignment>): PageResult<VehicleAssignmentResult> =
        PageResult(
            page.number + 1,
            page.size,
            page.totalPages,
            page.totalElements,
            page.map { toResult(it) }.toList()
        )

}
