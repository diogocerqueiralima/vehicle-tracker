package com.github.diogocerqueiralima.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a device with the specified ID is not found.
 */
public class DeviceNotFoundException extends RuntimeException {

    /**
     *
     * Constructs a new {@link DeviceNotFoundException} with a message indicating the missing device's ID.
     *
     * @param id the unique identifier of the device that was not found
     */
    public DeviceNotFoundException(UUID id) {
        super("Device with id " + id + " not found");
    }

}
