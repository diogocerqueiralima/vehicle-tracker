package com.github.diogocerqueiralima.asset.service.domain.exceptions;

import com.github.diogocerqueiralima.error.common.exceptions.exceptions.ConflictException;

import java.util.UUID;

/**
 * Exception thrown when a device-to-SIM card assignment cannot be persisted.
 */
public class SimCardAssignmentFailedException extends ConflictException {

    public SimCardAssignmentFailedException(UUID deviceId, UUID simCardId) {
        super("It was not possible to assign device " + deviceId + " to SIM card " + simCardId);
    }

}