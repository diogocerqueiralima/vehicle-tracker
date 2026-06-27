package com.github.diogocerqueiralima.asset.service.application.exceptions;

import com.github.diogocerqueiralima.asset.service.error.common.exceptions.NotFoundException;

import java.util.UUID;

/**
 * Exception thrown when no active assignment is found for a device and vehicle pair.
 */
public class VehicleAssignmentNotFoundException extends NotFoundException {

    public VehicleAssignmentNotFoundException(UUID deviceId, UUID vehicleId) {
        super("Active vehicle assignment not found for device id: " + deviceId + " and vehicle id: " + vehicleId);
    }

    public VehicleAssignmentNotFoundException(UUID deviceId) {
        super("Active vehicle assignment not found for device id: " + deviceId);
    }

}

