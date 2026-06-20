package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentHistoryResult;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.domain.assignments.VehicleAssignment;
import com.github.diogocerqueiralima.domain.assets.Device;
import com.github.diogocerqueiralima.domain.assets.Vehicle;
import org.springframework.data.domain.Page;

import java.time.Instant;

/**
 * Mapper for vehicle assignment conversions in the application layer.
 */
public final class VehicleAssignmentApplicationMapper {

    // Should not be instantiated
    private VehicleAssignmentApplicationMapper() {}

    /**
     *
     * Builds a domain vehicle assignment from the command payload and resolved assets.
     *
     * @param command assignment command payload.
     * @param device resolved device domain object.
     * @param vehicle resolved vehicle domain object.
     * @param assignedAt timestamp when assignment is created.
     * @return domain vehicle assignment ready for persistence.
     */
    public static VehicleAssignment toDomain(
            AssignDeviceToVehicleCommand command,
            Device device,
            Vehicle vehicle,
            Instant assignedAt
    ) {
        return new VehicleAssignment(
                device,
                vehicle,
                assignedAt,
                null,
                command.assignedBy(),
                null,
                null,
                command.installedBy(),
                command.notes()
        );
    }

    /**
     *
     * Builds an application result from a domain vehicle assignment.
     *
     * @param vehicleAssignment domain vehicle assignment.
     * @return assignment result contract.
     */
    public static VehicleAssignmentResult toResult(VehicleAssignment vehicleAssignment) {
        return new VehicleAssignmentResult(
                vehicleAssignment.getDevice().getId(),
                vehicleAssignment.getVehicle().getId(),
                vehicleAssignment.getAssignedAt(),
                vehicleAssignment.getAssignedBy(),
                vehicleAssignment.getUnassignedAt(),
                vehicleAssignment.getUnassignedBy(),
                vehicleAssignment.getRemovalReason(),
                vehicleAssignment.getInstalledBy(),
                vehicleAssignment.getNotes(),
                vehicleAssignment.isActive()
        );
    }

    /**
     *
     * Builds an updated domain vehicle assignment with unassignment data.
     *
     * @param command unassignment command payload.
     * @param activeAssignment currently active assignment.
     * @param unassignedAt timestamp when assignment is being closed.
     * @return domain assignment ready to be persisted as inactive.
     */
    public static VehicleAssignment toDomain(
            UnassignDeviceFromVehicleCommand command,
            VehicleAssignment activeAssignment,
            Instant unassignedAt
    ) {
        return new VehicleAssignment(
                activeAssignment.getId(),
                activeAssignment.getDevice(),
                activeAssignment.getVehicle(),
                activeAssignment.getAssignedAt(),
                unassignedAt,
                activeAssignment.getAssignedBy(),
                command.unassignedBy(),
                command.removalReason(),
                activeAssignment.getInstalledBy(),
                activeAssignment.getNotes()
        );
    }

    /**
     *
     * Converts a paginated list of domain vehicle assignments into an application result contract.
     *
     * @param page paginated list of domain vehicle assignments.
     * @return paginated result contract with the list of vehicle assignment results and pagination metadata.
     */
    public static VehicleAssignmentHistoryResult toResult(Page<VehicleAssignment> page) {
        return new VehicleAssignmentHistoryResult(
                page.get()
                        .map(VehicleAssignmentApplicationMapper::toResult)
                        .toList(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }

}

