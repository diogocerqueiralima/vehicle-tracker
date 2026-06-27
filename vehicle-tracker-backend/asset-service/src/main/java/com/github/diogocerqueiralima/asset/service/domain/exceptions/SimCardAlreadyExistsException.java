package com.github.diogocerqueiralima.asset.service.domain.exceptions;

import com.github.diogocerqueiralima.error.common.exceptions.exceptions.ConflictException;

/**
 * Exception thrown when attempting to persist a SIM card with an ICCID, MSISDN or IMSI already in use.
 */
public class SimCardAlreadyExistsException extends ConflictException {

    public SimCardAlreadyExistsException() {
        super("A SIM card with the provided ICCID, MSISDN or IMSI already exists.");
    }

}
