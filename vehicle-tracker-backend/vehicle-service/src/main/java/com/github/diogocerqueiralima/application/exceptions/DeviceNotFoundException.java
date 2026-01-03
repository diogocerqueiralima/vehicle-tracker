package com.github.diogocerqueiralima.application.exceptions;

import java.util.UUID;

public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(UUID id) {
        super("Device with id " + id + " not found");
    }

}
