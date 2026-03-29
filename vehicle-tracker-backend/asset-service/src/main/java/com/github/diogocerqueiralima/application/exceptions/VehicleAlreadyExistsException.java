package com.github.diogocerqueiralima.application.exceptions;

/**
 * Exception thrown when attempting to create a vehicle with unique fields already in use.
 */
public class VehicleAlreadyExistsException extends RuntimeException {

    public VehicleAlreadyExistsException(String message) {
        super(message);
    }

}

