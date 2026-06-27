package com.github.diogocerqueiralima.asset.service.domain.exceptions;

import com.github.diogocerqueiralima.asset.service.error.common.exceptions.ConflictException;

import java.util.UUID;

/**
 * Exception thrown when a device-to-vehicle assignment cannot be persisted.
 */
public class VehicleAssignmentFailedException extends ConflictException {

    public VehicleAssignmentFailedException(UUID deviceId, UUID vehicleId) {
        super("It was not possible to assign device " + deviceId + " to vehicle " + vehicleId);
    }

}