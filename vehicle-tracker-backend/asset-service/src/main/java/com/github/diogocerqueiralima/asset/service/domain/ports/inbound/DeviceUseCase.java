package com.github.diogocerqueiralima.asset.service.domain.ports.inbound;

import com.github.diogocerqueiralima.asset.service.application.commands.CreateOrUpdateDeviceCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.DeviceIsOwnedByUserCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException;
import com.github.diogocerqueiralima.error.common.exceptions.ForbiddenException;
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * Inbound port for device operations exposed to the presentation layer.
 */
@Validated
public interface DeviceUseCase {

    /**
     * Creates a new device or updates an existing one, both identified by the id supplied by the client.
     *
     * @param command the create-or-update device command.
     * @return the saved device result.
     */
    DeviceResult createOrUpdate(@Valid CreateOrUpdateDeviceCommand command);

    /**
     * Retrieves an existing device by id.
     *
     * @param command the get device by id command.
     * @return the retrieved device result.
     * @throws NotFoundException if the device is not found.
     */
    DeviceResult getById(@Valid GetDeviceByIdCommand command);

    /**
     * Retrieves a one-based pageNumber of devices.
     *
     * @param command the pageNumber request command.
     * @return paginated device result.
     */
    PageResult<DeviceResult> getPage(@Valid GetDevicePageCommand command);

    /**
     *
     * Checks if a device is owned by a specific user.
     *
     * @param command the command containing the device ID and user ID to check ownership.
     * @throws ForbiddenException if the device is not owned by the user.
     */
    void isOwnedBy(@Valid DeviceIsOwnedByUserCommand command);

}

