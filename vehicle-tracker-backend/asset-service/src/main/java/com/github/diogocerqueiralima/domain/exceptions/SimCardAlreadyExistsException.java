package com.github.diogocerqueiralima.domain.exceptions;

/**
 * Exception thrown when attempting to persist a SIM card with an ICCID, MSISDN or IMSI already in use.
 */
public class SimCardAlreadyExistsException extends RuntimeException {

    public SimCardAlreadyExistsException() {
        super("A SIM card with the provided ICCID, MSISDN or IMSI already exists.");
    }

}
