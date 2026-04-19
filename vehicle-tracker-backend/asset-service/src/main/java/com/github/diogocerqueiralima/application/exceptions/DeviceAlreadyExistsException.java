package com.github.diogocerqueiralima.application.exceptions;

/**
 * Exception thrown when attempting to create or update a device with unique fields already in use.
 */
public class DeviceAlreadyExistsException extends RuntimeException {

    public DeviceAlreadyExistsException(String message) {
        super(message);
    }

}

