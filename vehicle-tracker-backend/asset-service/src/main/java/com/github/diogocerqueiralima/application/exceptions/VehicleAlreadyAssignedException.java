package com.github.diogocerqueiralima.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when attempting to assign a vehicle that already has an active assignment.
 */
public class VehicleAlreadyAssignedException extends RuntimeException {

    public VehicleAlreadyAssignedException(UUID vehicleId) {
        super("Vehicle already assigned for id: " + vehicleId);
    }

}

