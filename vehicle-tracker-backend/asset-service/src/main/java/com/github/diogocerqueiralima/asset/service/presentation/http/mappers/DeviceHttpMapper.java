package com.github.diogocerqueiralima.asset.service.presentation.http.mappers;

import com.github.diogocerqueiralima.api.common.dto.PageDTO;
import com.github.diogocerqueiralima.asset.service.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateDeviceCommand;
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateDeviceRequestDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.DeviceDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UpdateDeviceRequestDTO;

import java.util.UUID;

/**
 * Mapper for device conversions in the HTTP presentation layer.
 */
public final class DeviceHttpMapper {

    // Should not be instantiated
    private DeviceHttpMapper() {}

    /**
     *
     * Builds a create command from an HTTP request payload.
     *
     * @param request request payload for device creation.
     * @return command consumed by the application layer.
     */
    public static CreateDeviceCommand toCreateCommand(CreateDeviceRequestDTO request) {
        return new CreateDeviceCommand(
                request.serialNumber(),
                request.model(),
                request.manufacturer(),
                request.imei(),
                request.ownerId()
        );
    }

    /**
     *
     * Builds an update command from an HTTP path identifier and a request payload.
     *
     * @param id device identifier from the request path.
     * @param request request payload for device update.
     * @return command consumed by the application layer.
     */
    public static UpdateDeviceCommand toUpdateCommand(UUID id, UpdateDeviceRequestDTO request) {
        return new UpdateDeviceCommand(
                id,
                request.serialNumber(),
                request.model(),
                request.manufacturer(),
                request.imei(),
                request.ownerId()
        );
    }

    /**
     * Builds a get-by-id command from an HTTP path identifier.
     *
     * @param id device identifier from the request path.
     * @param userId user identifier from the authenticated request context.
     * @param isAdmin flag indicating if the user has admin privileges.
     * @return command consumed by the application layer.
     */
    public static GetDeviceByIdCommand toGetByIdCommand(UUID id, UUID userId, boolean isAdmin) {
        return new GetDeviceByIdCommand(id, userId, isAdmin);
    }

    /**
     * Builds a get-pageNumber command from HTTP query params.
     *
     * @param pageNumber one-based pageNumber number.
     * @param pageSize requested pageNumber size.
     * @return command consumed by the application layer.
     */
    public static GetDevicePageCommand toGetPageCommand(int pageNumber, int pageSize, UUID userId) {
        return new GetDevicePageCommand(pageNumber, pageSize, userId);
    }

    /**
     *
     * Builds a transport DTO from an application result.
     *
     * @param result the application result to be converted into a transport DTO.
     * @return device DTO payload.
     */
    public static DeviceDTO toDTO(DeviceResult result) {
        return new DeviceDTO(
                result.id(),
                result.createdAt(),
                result.updatedAt(),
                result.serialNumber(),
                result.model(),
                result.manufacturer(),
                result.imei()
        );
    }

    /**
     * Converts a paginated application result into a paginated transport DTO.
     *
     * @param result paginated device results from application layer.
     * @return paginated device DTO payload.
     */
    public static PageDTO<DeviceDTO> toPageDTO(PageResult<DeviceResult> result) {
        return new PageDTO<>(
                result.pageNumber(),
                result.pageSize(),
                result.totalPages(),
                result.totalElements(),
                result.data().stream()
                        .map(DeviceHttpMapper::toDTO)
                        .toList()
        );
    }

}