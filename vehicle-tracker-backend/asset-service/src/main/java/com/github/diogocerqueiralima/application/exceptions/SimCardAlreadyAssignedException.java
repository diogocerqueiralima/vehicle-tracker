package com.github.diogocerqueiralima.application.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a SIM card already has an active assignment.
 */
public class SimCardAlreadyAssignedException extends RuntimeException {

    public SimCardAlreadyAssignedException(UUID id) {
        super("SIM card already assigned for id: " + id);
    }

}

