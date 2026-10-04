package com.github.diogocerqueiralima.asset.service.application.usecases

import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.error.common.exceptions.OperationFailedException
import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.mappers.SimCardAssignmentApplicationMapper
import com.github.diogocerqueiralima.asset.service.application.results.SimCardAssignmentResult
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.SimCardAssignmentUseCase
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.DevicePersistence
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.SimCardAssignmentPersistence
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.SimCardPersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Application-layer implementation that orchestrates SIM card assignment use cases.
 */
@Service
class SimCardAssignmentUseCaseImpl(
    private val devicePersistence: DevicePersistence,
    private val simCardPersistence: SimCardPersistence,
    private val simCardAssignmentPersistence: SimCardAssignmentPersistence
) : SimCardAssignmentUseCase {

    /**
     * Assigns a device to a SIM card after validating asset existence and active-assignment constraints.
     *
     * @param command assignment payload.
     * @return created assignment result.
     */
    @Transactional
    override fun assignDeviceToSimCard(command: AssignDeviceToSimCardCommand): SimCardAssignmentResult {

        val deviceId = command.deviceId
        val simCardId = command.simCardId
        val assignedBy = command.assignedBy

        // 1. Loads the device scoped to the authenticated owner and fails fast if it does not exist or is not owned.
        val device = devicePersistence.findByIdAndOwnerId(deviceId, assignedBy)
            ?: throw NotFoundException("Device not found for id: $deviceId")

        // 2. Loads the SIM card scoped to the authenticated owner and fails fast if it does not exist or is not owned.
        val simCard = simCardPersistence.findByIdAndOwnerId(simCardId, assignedBy)
            ?: throw NotFoundException("SIM card not found for id: $simCardId")

        // 3. Fails when either the device or the SIM card already has an active assignment.
        if (simCardAssignmentPersistence.hasActiveAssignmentForDevice(deviceId)
            || simCardAssignmentPersistence.hasActiveAssignmentForSimCard(simCardId)
        ) {
            throw OperationFailedException("It was not possible to assign device $deviceId to SIM card $simCardId")
        }

        // 4. Builds and saves the new assignment.
        val assignmentToSave = SimCardAssignmentApplicationMapper.toDomain(command, device, simCard, Instant.now())
        val savedAssignment = simCardAssignmentPersistence.save(assignmentToSave)

        // 5. Maps persisted assignment to application response contract.
        return SimCardAssignmentApplicationMapper.toResult(savedAssignment)
    }

    /**
     * Unassigns a device from a SIM card after validating assets and locating an active assignment.
     *
     * @param command unassignment payload.
     * @return updated assignment result.
     */
    @Transactional
    override fun unassignDeviceFromSimCard(command: UnassignDeviceFromSimCardCommand): SimCardAssignmentResult {

        val deviceId = command.deviceId
        val simCardId = command.simCardId
        val unassignedBy = command.unassignedBy

        // 1. Loads the device scoped to the authenticated owner and fails fast if it does not exist or is not owned.
        devicePersistence.findByIdAndOwnerId(deviceId, unassignedBy)
            ?: throw NotFoundException("Device not found for id: $deviceId")

        // 2. Loads the SIM card scoped to the authenticated owner and fails fast if it does not exist or is not owned.
        simCardPersistence.findByIdAndOwnerId(simCardId, unassignedBy)
            ?: throw NotFoundException("SIM card not found for id: $simCardId")

        // 3. Loads the active assignment for this exact device/SIM card pair.
        val activeAssignment = simCardAssignmentPersistence.findActiveByDeviceIdAndSimCardId(deviceId, simCardId)
            ?: throw NotFoundException(
                "Active SIM card assignment not found for device id: $deviceId and SIM id: $simCardId"
            )

        // 4. Creates the updated assignment aggregate with unassignment metadata.
        val assignmentToSave = SimCardAssignmentApplicationMapper.toDomain(command, activeAssignment, Instant.now())

        // 5. Persists the closed assignment and maps it to the application output contract.
        val savedAssignment = simCardAssignmentPersistence.save(assignmentToSave)

        return SimCardAssignmentApplicationMapper.toResult(savedAssignment)
    }

}
