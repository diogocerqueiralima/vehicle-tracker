package com.github.diogocerqueiralima.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a device cannot be found.
 */
public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(UUID id) {
        super("Device not found for id: " + id);
    }

}

