package com.github.diogocerqueiralima.presentation.mappers;

import com.github.diogocerqueiralima.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.application.commands.UpdateDeviceCommand;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.presentation.dto.CreateDeviceRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.DeviceDTO;
import com.github.diogocerqueiralima.presentation.dto.PageDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateDeviceRequestDTO;

import java.util.UUID;

/**
 * Mapper for device conversions in the presentation layer.
 */
public final class DevicePresentationMapper {

    // Should not be instantiated
    private DevicePresentationMapper() {}

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
                request.imei()
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
                request.imei()
        );
    }

    /**
     * Builds a get-by-id command from an HTTP path identifier.
     *
     * @param id device identifier from the request path.
     * @return command consumed by the application layer.
     */
    public static GetDeviceByIdCommand toGetByIdCommand(UUID id) {
        return new GetDeviceByIdCommand(id);
    }

    /**
     * Builds a get-page command from HTTP query params.
     *
     * @param pageNumber one-based page number.
     * @param pageSize requested page size.
     * @return command consumed by the application layer.
     */
    public static GetDevicePageCommand toGetPageCommand(int pageNumber, int pageSize) {
        return new GetDevicePageCommand(pageNumber, pageSize);
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
                result.totalElements(),
                result.totalPages(),
                result.data().stream()
                        .map(DevicePresentationMapper::toDTO)
                        .toList()
        );
    }

}

