package com.github.diogocerqueiralima.asset.service.application.exceptions;

import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException;

import java.util.UUID;

/**
 * Exception thrown when a device cannot be found.
 */
public class DeviceNotFoundException extends NotFoundException {

    public DeviceNotFoundException(UUID id) {
        super("Device not found for id: " + id);
    }

}

