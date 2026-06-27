package com.github.diogocerqueiralima.asset.service.application.exceptions;

import com.github.diogocerqueiralima.error.common.exceptions.exceptions.NotFoundException;

import java.util.UUID;

/**
 * Exception thrown when a SIM card cannot be found.
 */
public class SimCardNotFoundException extends NotFoundException {

    public SimCardNotFoundException(UUID id) {
        super("SIM card not found for id: " + id);
    }

}

