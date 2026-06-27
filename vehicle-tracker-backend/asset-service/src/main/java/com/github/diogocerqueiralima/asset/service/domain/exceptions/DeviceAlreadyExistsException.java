package com.github.diogocerqueiralima.asset.service.domain.exceptions;

import com.github.diogocerqueiralima.asset.service.error.common.exceptions.ConflictException;

/**
 * Exception thrown when attempting to persist a device with a serial number or IMEI already in use.
 */
public class DeviceAlreadyExistsException extends ConflictException {

    public DeviceAlreadyExistsException() {
        super("A device with the provided serial number or IMEI already exists.");
    }

}
