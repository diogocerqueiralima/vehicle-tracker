package com.github.diogocerqueiralima.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a vehicle cannot be found.
 */
public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(UUID id) {
        super("Vehicle not found for id: " + id);
    }

}

