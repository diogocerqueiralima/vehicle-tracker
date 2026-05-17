package com.github.diogocerqueiralima.presentation.http.mappers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.presentation.http.dto.AssignDeviceToVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.http.dto.UnassignDeviceFromVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.http.dto.VehicleAssignmentDTO;

import java.util.UUID;

/**
 * Mapper for vehicle assignment conversions in the HTTP presentation layer.
 */
public final class VehicleAssignmentHttpMapper {

    // Should not be instantiated
    private VehicleAssignmentHttpMapper() {}

    /**
     *
     * Builds an assignment command from an HTTP request payload.
     *
     * @param request request payload for creating a vehicle assignment.
     * @param assignedBy user identifier from the authentication context.
     * @return command consumed by the application layer.
     */
    public static AssignDeviceToVehicleCommand toAssignDeviceToVehicleCommand(
            AssignDeviceToVehicleRequestDTO request, UUID assignedBy
    ) {
        return new AssignDeviceToVehicleCommand(
                request.deviceId(),
                request.vehicleId(),
                assignedBy,
                request.installedBy(),
                request.notes()
        );
    }

    /**
     *
     * Builds a transport DTO from an application result.
     *
     * @param result assignment result from the application layer.
     * @return assignment DTO payload.
     */
    public static VehicleAssignmentDTO toDTO(VehicleAssignmentResult result) {
        return new VehicleAssignmentDTO(
                result.deviceId(),
                result.vehicleId(),
                result.assignedAt(),
                result.assignedBy(),
                result.unassignedAt(),
                result.unassignedBy(),
                result.removalReason(),
                result.installedBy(),
                result.notes(),
                result.active()
        );
    }

    /**
     *
     * Builds an unassignment command from an HTTP request payload.
     *
     * @param request request payload for closing a vehicle assignment.
     * @param unassignedBy user identifier from the authentication context.
     * @return command consumed by the application layer.
     */
    public static UnassignDeviceFromVehicleCommand toUnassignDeviceFromVehicleCommand(
            UnassignDeviceFromVehicleRequestDTO request,
            UUID unassignedBy
    ) {
        return new UnassignDeviceFromVehicleCommand(
                request.deviceId(),
                request.vehicleId(),
                unassignedBy,
                request.removalReason()
        );
    }

}