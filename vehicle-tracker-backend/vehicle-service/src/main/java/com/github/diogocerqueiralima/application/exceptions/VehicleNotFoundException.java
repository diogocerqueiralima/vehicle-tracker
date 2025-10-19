package com.github.diogocerqueiralima.application.exceptions;

import java.util.UUID;

public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(UUID id) {
        super("Vehicle with ID " + id + " not found.");
    }

}
