package com.github.diogocerqueiralima.domain.exceptions;

/**
 * Exception thrown when attempting to persist a device with a serial number or IMEI already in use.
 */
public class DeviceAlreadyExistsException extends RuntimeException {

    public DeviceAlreadyExistsException() {
        super("A device with the provided serial number or IMEI already exists.");
    }

}
