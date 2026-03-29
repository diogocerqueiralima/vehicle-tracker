package com.github.diogocerqueiralima.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when no active assignment is found for a device and vehicle pair.
 */
public class VehicleAssignmentNotFoundException extends RuntimeException {

    public VehicleAssignmentNotFoundException(UUID deviceId, UUID vehicleId) {
        super("Active vehicle assignment not found for device id: " + deviceId + " and vehicle id: " + vehicleId);
    }

}

