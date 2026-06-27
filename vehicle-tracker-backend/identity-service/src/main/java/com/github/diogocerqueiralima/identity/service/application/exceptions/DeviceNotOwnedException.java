package com.github.diogocerqueiralima.identity.service.application.exceptions;

import com.github.diogocerqueiralima.error.common.exceptions.ForbiddenException;

import java.util.UUID;

/**
 * Exception thrown when a device does not belong to the specified owner.
 */
public class DeviceNotOwnedException extends ForbiddenException {

    /**
     *
     * Constructs a new {@link DeviceNotOwnedException} with a message indicating the device ID and the owner ID that do not match.
     *
     * @param deviceId the unique identifier of the device that does not belong to the owner
     * @param ownerId the unique identifier of the owner that does not own the device
     */
    public DeviceNotOwnedException(UUID deviceId, UUID ownerId) {
        super("Device with id " + deviceId + " does not belong to owner with id " + ownerId);
    }

}
