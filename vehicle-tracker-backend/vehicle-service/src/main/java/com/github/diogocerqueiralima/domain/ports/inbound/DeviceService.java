package com.github.diogocerqueiralima.domain.ports.inbound;

import com.github.diogocerqueiralima.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.application.commands.LookupDeviceByIdCommand;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.presentation.context.ExecutionContext;
import com.github.diogocerqueiralima.application.exceptions.DeviceNotFoundException;

/**
 * Service interface for managing devices.
 */
public interface DeviceService {

    /**
     *
     * Creates a new device based on the provided command
     *
     * @param command the command containing device details
     * @param context the execution context
     * @return the result of the device creation
     */
    DeviceResult create(CreateDeviceCommand command, ExecutionContext context);

    /**
     *
     * Retrieves a device by its id based on the provided command
     *
     * @param command the command containing the device ID
     * @param context the execution context
     * @return the result of the device lookup
     * @throws DeviceNotFoundException if the device is not found
     */
    DeviceResult getById(LookupDeviceByIdCommand command, ExecutionContext context);

    /**
     *
     * Deletes a device by its ID based on the provided command.
     *
     * @param command the command containing the device ID
     * @param context the execution context
     * @throws DeviceNotFoundException if the device is not found
     */
    void delete(LookupDeviceByIdCommand command, ExecutionContext context);

}
