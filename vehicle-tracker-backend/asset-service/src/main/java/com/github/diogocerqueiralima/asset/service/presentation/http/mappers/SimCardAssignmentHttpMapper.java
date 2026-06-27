package com.github.diogocerqueiralima.asset.service.presentation.http.mappers;

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToSimCardCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromSimCardCommand;
import com.github.diogocerqueiralima.asset.service.application.results.SimCardAssignmentResult;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.AssignDeviceToSimCardRequestDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.SimCardAssignmentDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UnassignDeviceFromSimCardRequestDTO;

import java.util.UUID;

/**
 * Mapper for SIM card assignment conversions in the HTTP presentation layer.
 */
public final class SimCardAssignmentHttpMapper {

    // Should not be instantiated
    private SimCardAssignmentHttpMapper() {}

    /**
     *
     * Builds an assignment command from an HTTP request payload.
     *
     * @param request request payload for creating a SIM card assignment.
     * @param simCardId SIM card identifier from the request path.
     * @param assignedBy user identifier from the authentication context.
     * @return command consumed by the application layer.
     */
    public static AssignDeviceToSimCardCommand toAssignDeviceToSimCardCommand(
            AssignDeviceToSimCardRequestDTO request, UUID simCardId, UUID assignedBy
    ) {
        return new AssignDeviceToSimCardCommand(
                request.deviceId(),
                simCardId,
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
     * @param simCardId SIM card identifier from the request path.
     * @param unassignedBy user identifier from the authentication context.
     * @return command consumed by the application layer.
     */
    public static UnassignDeviceFromSimCardCommand toUnassignDeviceFromSimCardCommand(
            UnassignDeviceFromSimCardRequestDTO request,
            UUID simCardId,
            UUID unassignedBy
    ) {
        return new UnassignDeviceFromSimCardCommand(
                request.deviceId(),
                simCardId,
                unassignedBy,
                request.removalReason()
        );
    }

}