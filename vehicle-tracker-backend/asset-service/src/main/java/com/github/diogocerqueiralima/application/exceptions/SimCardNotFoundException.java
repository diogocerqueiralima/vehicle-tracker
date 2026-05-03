package com.github.diogocerqueiralima.application.exceptions;

/**
 * Exception thrown when a SIM card cannot be found.
 */
public class SimCardNotFoundException extends RuntimeException {

    public SimCardNotFoundException(String iccid) {
        super("Sim card not found for iccid: " + iccid);
    }

}

