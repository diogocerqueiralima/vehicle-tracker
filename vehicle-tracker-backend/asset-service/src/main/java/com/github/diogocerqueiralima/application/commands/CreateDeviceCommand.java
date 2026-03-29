package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotBlank;

/**
 * Command payload used by the presentation layer to request device creation.
 */
public record CreateDeviceCommand(

        @NotBlank(message = "serialNumber is required")
        String serialNumber,

        @NotBlank(message = "model is required")
        String model,

        @NotBlank(message = "manufacturer is required")
        String manufacturer,

        @NotBlank(message = "imei is required")
        String imei

) {}

