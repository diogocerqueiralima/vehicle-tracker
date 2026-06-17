package com.github.diogocerqueiralima.domain.exceptions;

/**
 * Exception thrown when attempting to persist a vehicle with a VIN or plate already in use.
 */
public class VehicleAlreadyExistsException extends RuntimeException {

    public VehicleAlreadyExistsException() {
        super("A vehicle with the provided VIN or plate already exists.");
    }

}
