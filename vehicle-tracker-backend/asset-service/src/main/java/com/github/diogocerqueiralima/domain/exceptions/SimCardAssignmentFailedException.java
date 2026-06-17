package com.github.diogocerqueiralima.domain.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a device-to-SIM card assignment cannot be persisted.
 */
public class SimCardAssignmentFailedException extends RuntimeException {

    public SimCardAssignmentFailedException(UUID deviceId, UUID simCardId) {
        super("It was not possible to assign device " + deviceId + " to SIM card " + simCardId);
    }

}