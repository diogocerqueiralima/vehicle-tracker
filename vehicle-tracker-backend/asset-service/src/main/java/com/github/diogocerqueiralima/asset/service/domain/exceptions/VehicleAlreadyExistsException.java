package com.github.diogocerqueiralima.asset.service.domain.exceptions;

import com.github.diogocerqueiralima.error.common.exceptions.exceptions.ConflictException;

/**
 * Exception thrown when attempting to persist a vehicle with a VIN or plate already in use.
 */
public class VehicleAlreadyExistsException extends ConflictException {

    public VehicleAlreadyExistsException() {
        super("A vehicle with the provided VIN or plate already exists.");
    }

}
