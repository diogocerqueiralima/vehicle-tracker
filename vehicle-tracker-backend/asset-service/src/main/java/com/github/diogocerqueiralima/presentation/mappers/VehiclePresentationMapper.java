package com.github.diogocerqueiralima.presentation.mappers;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetVehiclePageCommand;
import com.github.diogocerqueiralima.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.presentation.dto.CreateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.PageDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.VehicleDTO;

import java.util.UUID;

/**
 * Mapper for vehicle conversions in the presentation layer.
 */
public final class VehiclePresentationMapper {

    // Should not be instantiated
    private VehiclePresentationMapper() {}

    /**
     *
     * Builds a create command from an HTTP request payload.
     *
     * @param request request payload for vehicle creation.
     * @return command consumed by the application layer.
     */
    public static CreateVehicleCommand toCreateCommand(CreateVehicleRequestDTO request, UUID userId) {
        return new CreateVehicleCommand(
                request.vin(),
                request.plate(),
                request.model(),
                request.manufacturer(),
                request.manufacturingDate(),
                userId
        );
    }

    /**
     *
     * Builds an update command from an HTTP path identifier and a request payload.
     *
     * @param id vehicle identifier from the request path.
     * @param request request payload for vehicle update.
     * @return command consumed by the application layer.
     */
    public static UpdateVehicleCommand toUpdateCommand(UUID id, UpdateVehicleRequestDTO request, UUID userId) {
        return new UpdateVehicleCommand(
                id,
                request.vin(),
                request.plate(),
                request.model(),
                request.manufacturer(),
                request.manufacturingDate(),
                userId
        );
    }

    /**
     * Builds a get-by-id command from an HTTP path identifier.
     *
     * @param id vehicle identifier from the request path.
     * @return command consumed by the application layer.
     */
    public static GetVehicleByIdCommand toGetByIdCommand(UUID id, UUID userId) {
        return new GetVehicleByIdCommand(id, userId);
    }

    /**
     * Builds a get-page command from HTTP query params.
     *
     * @param pageNumber one-based page number.
     * @param pageSize requested page size.
     * @return command consumed by the application layer.
     */
    public static GetVehiclePageCommand toGetPageCommand(int pageNumber, int pageSize, UUID userId) {
        return new GetVehiclePageCommand(pageNumber, pageSize, userId);
    }

    /**
     *
     * Builds a transport DTO from an application result.
     *
     * @param result the application result to be converted into a transport DTO.
     * @return vehicle DTO payload.
     */
    public static VehicleDTO toDTO(VehicleResult result) {
        return new VehicleDTO(
                result.id(),
                result.createdAt(),
                result.updatedAt(),
                result.vin(),
                result.plate(),
                result.model(),
                result.manufacturer(),
                result.manufacturingDate()
        );
    }

    /**
     * Converts a paginated application result into a paginated transport DTO.
     *
     * @param result paginated vehicle results from application layer.
     * @return paginated vehicle DTO payload.
     */
    public static PageDTO<VehicleDTO> toPageDTO(PageResult<VehicleResult> result) {
        return new PageDTO<>(
                result.pageNumber(),
                result.pageSize(),
                result.totalElements(),
                result.totalPages(),
                result.data().stream()
                        .map(VehiclePresentationMapper::toDTO)
                        .toList()
        );
    }

}

