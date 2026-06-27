package com.github.diogocerqueiralima.asset.service.application.exceptions;

import com.github.diogocerqueiralima.asset.service.error.common.exceptions.NotFoundException;

import java.util.UUID;

/**
 * Exception thrown when a vehicle cannot be found.
 */
public class VehicleNotFoundException extends NotFoundException {

    public VehicleNotFoundException(UUID id) {
        super("Vehicle not found for id: " + id);
    }

}

