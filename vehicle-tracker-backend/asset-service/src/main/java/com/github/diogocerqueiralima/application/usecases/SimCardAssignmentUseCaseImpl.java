package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromSimCardCommand;
import com.github.diogocerqueiralima.application.exceptions.DeviceAlreadyAssignedException;
import com.github.diogocerqueiralima.application.exceptions.DeviceNotFoundException;
import com.github.diogocerqueiralima.application.exceptions.SimCardAlreadyAssignedException;
import com.github.diogocerqueiralima.application.exceptions.SimCardAssignmentNotFoundException;
import com.github.diogocerqueiralima.application.exceptions.SimCardNotFoundException;
import com.github.diogocerqueiralima.application.mappers.SimCardAssignmentApplicationMapper;
import com.github.diogocerqueiralima.application.ports.inbound.SimCardAssignmentUseCase;
import com.github.diogocerqueiralima.application.ports.outbound.DevicePersistence;
import com.github.diogocerqueiralima.application.ports.outbound.SimCardAssignmentPersistence;
import com.github.diogocerqueiralima.application.ports.outbound.SimCardPersistence;
import com.github.diogocerqueiralima.application.results.SimCardAssignmentResult;
import com.github.diogocerqueiralima.domain.assets.SimCard;
import com.github.diogocerqueiralima.domain.assignments.SimCardAssignment;
import com.github.diogocerqueiralima.domain.assets.Device;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Application-layer implementation that orchestrates SIM card assignment use cases.
 */
@Service
public class SimCardAssignmentUseCaseImpl implements SimCardAssignmentUseCase {

    private final DevicePersistence devicePersistence;
    private final SimCardPersistence simCardPersistence;
    private final SimCardAssignmentPersistence simCardAssignmentPersistence;

    public SimCardAssignmentUseCaseImpl(
            DevicePersistence devicePersistence,
            SimCardPersistence simCardPersistence,
            SimCardAssignmentPersistence simCardAssignmentPersistence
    ) {
        this.devicePersistence = devicePersistence;
        this.simCardPersistence = simCardPersistence;
        this.simCardAssignmentPersistence = simCardAssignmentPersistence;
    }

    /**
     * Assigns a device to a SIM card after validating asset existence and active-assignment constraints.
     *
     * @param command assignment payload.
     * @return created assignment result.
     */
    @Override
    @Transactional
    public SimCardAssignmentResult assignDeviceToSimCard(AssignDeviceToSimCardCommand command) {

        UUID deviceId = command.deviceId();
        UUID simCardId = command.simCardId();

        // 1. Loads the device and fails fast if it does not exist.
        Device device = devicePersistence.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        // 2. Loads the SIM card and fails fast if it does not exist.
        SimCard simCard = simCardPersistence.findById(simCardId)
                .orElseThrow(() -> new SimCardNotFoundException(simCardId));

        // 3. Rejects assignment when the device already has an active SIM card assignment.
        if (simCardAssignmentPersistence.existsActiveByDeviceId(deviceId)) {
            throw new DeviceAlreadyAssignedException(deviceId);
        }

        // 4. Rejects assignment when the SIM card already has an active device assignment.
        if (simCardAssignmentPersistence.existsActiveBySimCardId(simCardId)) {
            throw new SimCardAlreadyAssignedException(simCardId);
        }

        // 5. Builds and saves the new assignment.
        SimCardAssignment assignmentToSave = SimCardAssignmentApplicationMapper.toDomain(
                command,
                device,
                simCard,
                Instant.now()
        );

        SimCardAssignment savedAssignment = simCardAssignmentPersistence.save(assignmentToSave);

        // 6. Maps persisted assignment to application response contract.
        return SimCardAssignmentApplicationMapper.toResult(savedAssignment);
    }

    /**
     * Unassigns a device from a SIM card after validating assets and locating an active assignment.
     *
     * @param command unassignment payload.
     * @return updated assignment result.
     */
    @Override
    @Transactional
    public SimCardAssignmentResult unassignDeviceFromSimCard(UnassignDeviceFromSimCardCommand command) {

        UUID deviceId = command.deviceId();
        UUID simCardId = command.simCardId();

        // 1. Loads the device and fails fast if it does not exist.
        devicePersistence.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        // 2. Loads the SIM card and fails fast if it does not exist.
        simCardPersistence.findById(simCardId)
                .orElseThrow(() -> new SimCardNotFoundException(simCardId));

        // 3. Loads the active assignment for this exact device/SIM card pair.
        SimCardAssignment activeAssignment = simCardAssignmentPersistence
                .findActiveByDeviceIdAndSimCardId(deviceId, simCardId)
                .orElseThrow(() -> new SimCardAssignmentNotFoundException(deviceId, simCardId));

        // 4. Creates the updated assignment aggregate with unassignment metadata.
        SimCardAssignment assignmentToSave = SimCardAssignmentApplicationMapper.toDomain(
                command,
                activeAssignment,
                Instant.now()
        );

        // 5. Persists the closed assignment and maps it to the application output contract.
        SimCardAssignment savedAssignment = simCardAssignmentPersistence.save(assignmentToSave);
        return SimCardAssignmentApplicationMapper.toResult(savedAssignment);
    }

}

