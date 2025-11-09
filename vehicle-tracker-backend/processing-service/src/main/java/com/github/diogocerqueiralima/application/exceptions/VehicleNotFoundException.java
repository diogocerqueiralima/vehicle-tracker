package com.github.diogocerqueiralima.application.exceptions;

import java.util.UUID;

public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(UUID deviceId) {
        super("Vehicle not found for device ID: " + deviceId);
    }

}
