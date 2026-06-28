package com.github.diogocerqueiralima.asset.service.presentation.http.mappers;

import com.github.diogocerqueiralima.api.common.dto.PageDTO;
import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleAssignmentHistoryCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.AssignDeviceToVehicleRequestDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UnassignDeviceFromVehicleRequestDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.VehicleAssignmentDTO;

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
     * @param vehicleId vehicle identifier from the request path.
     * @param assignedBy user identifier from the authentication context.
     * @return command consumed by the application layer.
     */
    public static AssignDeviceToVehicleCommand toAssignDeviceToVehicleCommand(
            AssignDeviceToVehicleRequestDTO request, UUID vehicleId, UUID assignedBy
    ) {
        return new AssignDeviceToVehicleCommand(
                request.deviceId(),
                vehicleId,
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
     * @param vehicleId vehicle identifier from the request path.
     * @param unassignedBy user identifier from the authentication context.
     * @return command consumed by the application layer.
     */
    public static UnassignDeviceFromVehicleCommand toUnassignDeviceFromVehicleCommand(
            UnassignDeviceFromVehicleRequestDTO request,
            UUID vehicleId,
            UUID unassignedBy
    ) {
        return new UnassignDeviceFromVehicleCommand(
                request.deviceId(),
                vehicleId,
                unassignedBy,
                request.removalReason()
        );
    }

    /**
     *
     * Builds a command to retrieve the assignment history of a vehicle.
     *
     * @param vehicleId the unique identifier of the vehicle for which the assignment history is being requested.
     * @param userId the unique identifier of the user making the request, used for authorization and auditing purposes.
     * @param pageNumber the page number of the assignment history results to retrieve, used for pagination of results.
     * @param pageSize the number of assignment history results to retrieve per page, used for pagination of results.
     * @return a command consumed by the application layer to fetch the assignment history of the specified vehicle.
     */
    public static GetVehicleAssignmentHistoryCommand toGetVehicleAssignmentHistoryCommand(
            UUID vehicleId,
            UUID userId,
            int pageNumber,
            int pageSize
    ) {
        return new GetVehicleAssignmentHistoryCommand(vehicleId, userId, pageNumber, pageSize);
    }

    /**
     *
     * Builds a paginated DTO from an application page result.
     *
     * @param result the paginated result from the application layer containing a list of vehicle assignment results and pagination metadata.
     * @return a paginated DTO containing the list of vehicle assignment DTOs and pagination metadata to be returned in the HTTP response.
     */
    public static PageDTO<VehicleAssignmentDTO> toPageDTO(PageResult<VehicleAssignmentResult> result) {
        return new PageDTO<>(
                result.pageNumber(),
                result.pageSize(),
                result.totalPages(),
                result.totalElements(),
                result.data()
                        .stream()
                        .map(VehicleAssignmentHttpMapper::toDTO)
                        .toList()
        );
    }

}