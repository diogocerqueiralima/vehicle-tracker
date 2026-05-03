package com.github.diogocerqueiralima.presentation.mappers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardAssignmentResult;
import com.github.diogocerqueiralima.presentation.dto.AssignDeviceToSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.SimCardAssignmentDTO;
import com.github.diogocerqueiralima.presentation.dto.UnassignDeviceFromSimCardRequestDTO;

import java.util.UUID;

/**
 * Mapper for SIM card assignment conversions in the presentation layer.
 */
public final class SimCardAssignmentPresentationMapper {

    // Should not be instantiated
    private SimCardAssignmentPresentationMapper() {}

    /**
     *
     * Builds an assignment command from an HTTP request payload.
     *
     * @param request request payload for creating a SIM card assignment.
     * @param assignedBy user identifier from the authentication context.
     * @return command consumed by the application layer.
     */
    public static AssignDeviceToSimCardCommand toAssignDeviceToSimCardCommand(
            AssignDeviceToSimCardRequestDTO request, UUID assignedBy
    ) {
        return new AssignDeviceToSimCardCommand(
                request.deviceId(),
                request.simCardId(),
                assignedBy
        );
    }

    /**
     *
     * Builds a transport DTO from an application result.
     *
     * @param result assignment result from the application layer.
     * @return assignment DTO payload.
     */
    public static SimCardAssignmentDTO toDTO(SimCardAssignmentResult result) {
        return new SimCardAssignmentDTO(
                result.deviceId(),
                result.simCardId(),
                result.assignedAt(),
                result.assignedBy(),
                result.unassignedAt(),
                result.unassignedBy(),
                result.removalReason(),
                result.active()
        );
    }

    /**
     *
     * Builds an unassignment command from an HTTP request payload.
     *
     * @param request request payload for closing a SIM card assignment.
     * @param unassignedBy user identifier from the authentication context.
     * @return command consumed by the application layer.
     */
    public static UnassignDeviceFromSimCardCommand toUnassignDeviceFromSimCardCommand(
            UnassignDeviceFromSimCardRequestDTO request,
            UUID unassignedBy
    ) {
        return new UnassignDeviceFromSimCardCommand(
                request.deviceId(),
                request.simCardId(),
                unassignedBy,
                request.removalReason()
        );
    }

}

