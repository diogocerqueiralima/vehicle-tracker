package com.github.diogocerqueiralima.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a SIM card cannot be found.
 */
public class SimCardNotFoundException extends RuntimeException {

    public SimCardNotFoundException(UUID id) {
        super("SIM card not found for id: " + id);
    }

}

