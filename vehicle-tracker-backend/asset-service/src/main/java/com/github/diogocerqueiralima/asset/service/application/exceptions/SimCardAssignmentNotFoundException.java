package com.github.diogocerqueiralima.asset.service.application.exceptions;

import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException;

import java.util.UUID;

/**
 * Exception thrown when no active assignment is found for a device and SIM card pair.
 */
public class SimCardAssignmentNotFoundException extends NotFoundException {

    public SimCardAssignmentNotFoundException(UUID deviceId, UUID simCardId) {
        super("Active SIM card assignment not found for device id: " + deviceId + " and SIM id: " + simCardId);
    }

}

