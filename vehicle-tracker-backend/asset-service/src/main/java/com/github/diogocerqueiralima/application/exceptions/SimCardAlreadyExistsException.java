package com.github.diogocerqueiralima.application.exceptions;

/**
 * Exception thrown when attempting to create or update a SIM card with unique fields already in use.
 */
public class SimCardAlreadyExistsException extends RuntimeException {

    public SimCardAlreadyExistsException(String message) {
        super(message);
    }

}

