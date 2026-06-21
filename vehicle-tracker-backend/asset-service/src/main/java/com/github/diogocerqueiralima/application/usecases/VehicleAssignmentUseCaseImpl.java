package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleAssignmentByDeviceIdCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleAssignmentHistoryCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.application.exceptions.DeviceNotFoundException;
import com.github.diogocerqueiralima.application.exceptions.VehicleAssignmentNotFoundException;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.application.mappers.VehicleAssignmentApplicationMapper;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.domain.ports.inbound.VehicleAssignmentUseCase;
import com.github.diogocerqueiralima.domain.ports.outbound.DevicePersistence;
import com.github.diogocerqueiralima.domain.ports.outbound.VehicleAssignmentPersistence;
import com.github.diogocerqueiralima.domain.ports.outbound.VehiclePersistence;
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

        // 3. Builds and saves the new assignment.
        VehicleAssignment assignmentToSave = VehicleAssignmentApplicationMapper.toDomain(
                command,
                device,
                vehicle,
                Instant.now()
        );

        VehicleAssignment savedAssignment = vehicleAssignmentPersistence.save(assignmentToSave);

        // 4. Maps persisted assignment to application response contract.
        return VehicleAssignmentApplicationMapper.toResult(savedAssignment);
    }

    @Override
    @Transactional
    public VehicleAssignmentResult unassignDeviceFromVehicle(UnassignDeviceFromVehicleCommand command) {

        UUID deviceId = command.deviceId();
        UUID vehicleId = command.vehicleId();

        // 1. Loads the active assignment for this exact device/vehicle pair.
        VehicleAssignment activeAssignment = vehicleAssignmentPersistence
                .findActiveByDeviceIdAndVehicleId(deviceId, vehicleId)
                .orElseThrow(() -> new VehicleAssignmentNotFoundException(deviceId, vehicleId));

        // 2. Check if the user is the owner of the vehicle
        if (!vehiclePersistence.isOwner(vehicleId, command.unassignedBy())) {
            throw new VehicleNotFoundException(vehicleId);
        }

        // 3. Check if the user is the owner of the device
        if (!devicePersistence.isOwner(deviceId, command.unassignedBy())) {
            throw new DeviceNotFoundException(deviceId);
        }

        // 2. Creates the updated assignment aggregate with unassignment metadata.
        VehicleAssignment assignmentToSave = VehicleAssignmentApplicationMapper.toDomain(
                command,
                activeAssignment,
                Instant.now()
        );

        // 3. Persists the closed assignment and maps it to the application output contract.
        VehicleAssignment savedAssignment = vehicleAssignmentPersistence.save(assignmentToSave);
        return VehicleAssignmentApplicationMapper.toResult(savedAssignment);
    }

    @Override
    public VehicleAssignmentResult getVehicleAssignmentByDeviceId(GetVehicleAssignmentByDeviceIdCommand command) {

        UUID deviceId = command.deviceId();

        return vehicleAssignmentPersistence.findActiveByDeviceId(deviceId)
                .map(VehicleAssignmentApplicationMapper::toResult)
                .orElseThrow(() -> new VehicleAssignmentNotFoundException(deviceId));
    }

    @Override
    public PageResult<VehicleAssignmentResult> getVehicleAssignmentHistory(GetVehicleAssignmentHistoryCommand command) {

        UUID vehicleId = command.vehicleId();
        UUID userId = command.userId();
        int page = command.pageNumber();
        int pageSize = command.pageSize();

        return VehicleAssignmentApplicationMapper.toResult(
                vehicleAssignmentPersistence.findHistory(vehicleId, userId, page - 1, pageSize)
        );
    }

}

