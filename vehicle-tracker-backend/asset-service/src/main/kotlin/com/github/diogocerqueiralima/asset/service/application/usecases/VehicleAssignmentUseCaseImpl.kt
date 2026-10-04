package com.github.diogocerqueiralima.asset.service.application.usecases

import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.error.common.exceptions.OperationFailedException
import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleAssignmentByDeviceIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleAssignmentHistoryCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.mappers.VehicleAssignmentApplicationMapper
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.VehicleAssignmentUseCase
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.DevicePersistence
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehicleAssignmentPersistence
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehiclePersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Application-layer implementation that orchestrates vehicle assignment use cases.
 */
@Service
class VehicleAssignmentUseCaseImpl(
    private val devicePersistence: DevicePersistence,
    private val vehiclePersistence: VehiclePersistence,
    private val vehicleAssignmentPersistence: VehicleAssignmentPersistence
) : VehicleAssignmentUseCase {

    @Transactional
    override fun assignDeviceToVehicle(command: AssignDeviceToVehicleCommand): VehicleAssignmentResult {

        val deviceId = command.deviceId
        val vehicleId = command.vehicleId
        val assignedBy = command.assignedBy

        // 1. Loads the device scoped to the authenticated owner and fails fast if it does not exist or is not owned.
        val device = devicePersistence.findByIdAndOwnerId(deviceId, assignedBy)
            ?: throw NotFoundException("Device not found for id: $deviceId")

        // 2. Loads the vehicle scoped to the authenticated owner and fails fast if it does not exist or is not owned.
        val vehicle = vehiclePersistence.findByIdAndOwnerId(vehicleId, assignedBy)
            ?: throw NotFoundException("Vehicle not found for id: $vehicleId")

        // 3. Fails when either the device or the vehicle already has an active assignment.
        if (vehicleAssignmentPersistence.hasActiveAssignmentForDevice(deviceId)
            || vehicleAssignmentPersistence.hasActiveAssignmentForVehicle(vehicleId)
        ) {
            throw OperationFailedException("It was not possible to assign device $deviceId to vehicle $vehicleId")
        }

        // 4. Builds and saves the new assignment.
        val assignmentToSave = VehicleAssignmentApplicationMapper.toDomain(command, device, vehicle, Instant.now())
        val savedAssignment = vehicleAssignmentPersistence.save(assignmentToSave)

        // 5. Maps persisted assignment to application response contract.
        return VehicleAssignmentApplicationMapper.toResult(savedAssignment)
    }

    @Transactional
    override fun unassignDeviceFromVehicle(command: UnassignDeviceFromVehicleCommand): VehicleAssignmentResult {

        val deviceId = command.deviceId
        val vehicleId = command.vehicleId

        // 1. Loads the active assignment for this exact device/vehicle pair.
        val activeAssignment = vehicleAssignmentPersistence.findActiveByDeviceIdAndVehicleId(deviceId, vehicleId)
            ?: throw NotFoundException(
                "Active vehicle assignment not found for device id: $deviceId and vehicle id: $vehicleId"
            )

        // 2. Check if the user is the owner of the vehicle
        if (!vehiclePersistence.isOwner(vehicleId, command.unassignedBy)) {
            throw NotFoundException("Vehicle not found for id: $vehicleId")
        }

        // 3. Check if the user is the owner of the device
        if (!devicePersistence.isOwner(deviceId, command.unassignedBy)) {
            throw NotFoundException("Device not found for id: $deviceId")
        }

        // 4. Creates the updated assignment aggregate with unassignment metadata.
        val assignmentToSave = VehicleAssignmentApplicationMapper.toDomain(command, activeAssignment, Instant.now())

        // 5. Persists the closed assignment and maps it to the application output contract.
        val savedAssignment = vehicleAssignmentPersistence.save(assignmentToSave)

        return VehicleAssignmentApplicationMapper.toResult(savedAssignment)
    }

    override fun getVehicleAssignmentByDeviceId(command: GetVehicleAssignmentByDeviceIdCommand): VehicleAssignmentResult {

        val deviceId = command.deviceId

        return vehicleAssignmentPersistence.findActiveByDeviceId(deviceId)
            ?.let(VehicleAssignmentApplicationMapper::toResult)
            ?: throw NotFoundException("Active vehicle assignment not found for device id: $deviceId")
    }

    override fun getVehicleAssignmentHistory(command: GetVehicleAssignmentHistoryCommand): PageResult<VehicleAssignmentResult> =
        VehicleAssignmentApplicationMapper.toResult(
            vehicleAssignmentPersistence.findHistory(
                command.vehicleId, command.userId, command.pageNumber - 1, command.pageSize
            )
        )

}
