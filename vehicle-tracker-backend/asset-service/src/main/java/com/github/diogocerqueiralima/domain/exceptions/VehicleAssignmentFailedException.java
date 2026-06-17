package com.github.diogocerqueiralima.domain.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a device-to-vehicle assignment cannot be persisted.
 */
public class VehicleAssignmentFailedException extends RuntimeException {

    public VehicleAssignmentFailedException(UUID deviceId, UUID vehicleId) {
        super("It was not possible to assign device " + deviceId + " to vehicle " + vehicleId);
    }

}