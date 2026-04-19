package com.github.diogocerqueiralima.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when attempting to assign a device that already has an active assignment.
 */
public class DeviceAlreadyAssignedException extends RuntimeException {

    public DeviceAlreadyAssignedException(UUID deviceId) {
        super("Device already assigned for id: " + deviceId);
    }

}

