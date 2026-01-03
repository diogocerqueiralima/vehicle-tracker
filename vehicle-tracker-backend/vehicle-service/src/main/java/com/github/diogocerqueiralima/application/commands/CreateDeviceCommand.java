package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 *
 * Command to create a new device.
 *
 * @param imei the imei
 * @param serialNumber the serial number
 * @param manufacturer the manufacturer
 * @param vehicleId the vehicle unique identifier
 */
public record CreateDeviceCommand(
        @NotBlank String imei, @NotBlank String serialNumber, @NotBlank String manufacturer, @NotNull UUID vehicleId
) {}
