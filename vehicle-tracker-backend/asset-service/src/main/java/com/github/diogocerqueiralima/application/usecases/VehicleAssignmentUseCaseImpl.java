package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.application.exceptions.DeviceAlreadyAssignedException;
import com.github.diogocerqueiralima.application.exceptions.DeviceNotFoundException;
import com.github.diogocerqueiralima.application.exceptions.VehicleAssignmentNotFoundException;
import com.github.diogocerqueiralima.application.exceptions.VehicleAlreadyAssignedException;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.application.mappers.VehicleAssignmentApplicationMapper;
import com.github.diogocerqueiralima.application.ports.inbound.VehicleAssignmentUseCase;
import com.github.diogocerqueiralima.application.ports.outbound.DevicePersistence;
import com.github.diogocerqueiralima.application.ports.outbound.VehicleAssignmentPersistence;
import com.github.diogocerqueiralima.application.ports.outbound.VehiclePersistence;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.domain.assignments.VehicleAssignment;
import com.github.diogocerqueiralima.domain.assets.Device;
import com.github.diogocerqueiralima.domain.assets.Vehicle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Application-layer implementation that orchestrates vehicle assignment use cases.
 */
@Service
public class VehicleAssignmentUseCaseImpl implements VehicleAssignmentUseCase {

    private final DevicePersistence devicePersistence;
    private final VehiclePersistence vehiclePersistence;
    private final VehicleAssignmentPersistence vehicleAssignmentPersistence;

    public VehicleAssignmentUseCaseImpl(
            DevicePersistence devicePersistence,
            VehiclePersistence vehiclePersistence,
            VehicleAssignmentPersistence vehicleAssignmentPersistence
    ) {
        this.devicePersistence = devicePersistence;
        this.vehiclePersistence = vehiclePersistence;
        this.vehicleAssignmentPersistence = vehicleAssignmentPersistence;
    }

    /**
     * Assigns a device to a vehicle after validating asset existence and active-assignment constraints.
     *
     * @param command assignment payload.
     * @return created assignment result.
     */
    @Override
    @Transactional
    public VehicleAssignmentResult assignDeviceToVehicle(AssignDeviceToVehicleCommand command) {

        UUID deviceId = command.deviceId();
        UUID vehicleId = command.vehicleId();
        UUID assignedBy = command.assignedBy();

        // 1. Loads the device scoped to the authenticated owner and fails fast if it does not exist or is not owned.
        Device device = devicePersistence.findByIdAndOwnerId(deviceId, assignedBy)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        // 2. Loads the vehicle scoped to the authenticated owner and fails fast if it does not exist or is not owned.
        Vehicle vehicle = vehiclePersistence.findByIdAndOwnerId(vehicleId, assignedBy)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        // 3. Rejects assignment when the device already has an active assignment.
        if (vehicleAssignmentPersistence.existsActiveByDeviceId(deviceId)) {
            throw new DeviceAlreadyAssignedException(deviceId);
        }

        // 4. Rejects assignment when the vehicle already has an active assignment.
        if (vehicleAssignmentPersistence.existsActiveByVehicleId(vehicleId)) {
            throw new VehicleAlreadyAssignedException(vehicleId);
        }

        // 5. Builds and saves the new assignment.
        VehicleAssignment assignmentToSave = VehicleAssignmentApplicationMapper.toDomain(
                command,
                device,
                vehicle,
                Instant.now()
        );

        VehicleAssignment savedAssignment = vehicleAssignmentPersistence.save(assignmentToSave);

        // 6. Maps persisted assignment to application response contract.
        return VehicleAssignmentApplicationMapper.toResult(savedAssignment);
    }

    /**
     * Unassigns a device from a vehicle after validating assets and locating an active assignment.
     *
     * @param command unassignment payload.
     * @return updated assignment result.
     */
    @Override
    @Transactional
    public VehicleAssignmentResult unassignDeviceFromVehicle(UnassignDeviceFromVehicleCommand command) {

        UUID deviceId = command.deviceId();
        UUID vehicleId = command.vehicleId();
        UUID unassignedBy = command.unassignedBy();

        // 1. Loads the device scoped to the authenticated owner and fails fast if it does not exist or is not owned.
        Device device = devicePersistence.findByIdAndOwnerId(deviceId, unassignedBy)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        // 2. Loads the vehicle scoped to the authenticated owner and fails fast if it does not exist or is not owned.
        Vehicle vehicle = vehiclePersistence.findByIdAndOwnerId(vehicleId, unassignedBy)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        // 3. Loads the active assignment for this exact device/vehicle pair.
        VehicleAssignment activeAssignment = vehicleAssignmentPersistence
                .findActiveByDeviceIdAndVehicleId(deviceId, vehicleId)
                .orElseThrow(() -> new VehicleAssignmentNotFoundException(deviceId, vehicleId));

        // 4. Creates the updated assignment aggregate with unassignment metadata.
        VehicleAssignment assignmentToSave = VehicleAssignmentApplicationMapper.toDomain(
                command,
                activeAssignment,
                Instant.now()
        );

        // 5. Persists the closed assignment and maps it to the application output contract.
        VehicleAssignment savedAssignment = vehicleAssignmentPersistence.save(assignmentToSave);
        return VehicleAssignmentApplicationMapper.toResult(savedAssignment);
    }

}

