package com.github.diogocerqueiralima.domain.ports.inbound;

import com.github.diogocerqueiralima.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.application.commands.UpdateDeviceCommand;
import com.github.diogocerqueiralima.application.exceptions.DeviceAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.DeviceNotFoundException;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.application.results.PageResult;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * Inbound port for device operations exposed to the presentation layer.
 */
@Validated
public interface DeviceUseCase {

    /**
     * Creates a device from the supplied command payload.
     *
     * @param command the create device command.
     * @return the created device result.
     * @throws DeviceAlreadyExistsException if a device with the same serial number or IMEI already exists.
     */
    DeviceResult create(@Valid CreateDeviceCommand command);

    /**
     * Updates an existing device identified by id.
     *
     * @param command the update device command.
     * @return the updated device result.
     * @throws DeviceNotFoundException if the device is not found.
     * @throws DeviceAlreadyExistsException if the updated serial number or IMEI already exists for another device.
     */
    DeviceResult update(@Valid UpdateDeviceCommand command);

    /**
     * Retrieves an existing device by id.
     *
     * @param command the get device by id command.
     * @return the retrieved device result.
     * @throws DeviceNotFoundException if the device is not found.
     */
    DeviceResult getById(@Valid GetDeviceByIdCommand command);

    /**
     * Retrieves a one-based page of devices.
     *
     * @param command the page request command.
     * @return paginated device result.
     */
    PageResult<DeviceResult> getPage(@Valid GetDevicePageCommand command);

}

