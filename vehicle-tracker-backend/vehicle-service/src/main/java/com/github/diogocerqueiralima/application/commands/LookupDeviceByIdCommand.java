package com.github.diogocerqueiralima.application.commands;

import java.util.UUID;

/**
 *
 * Command to look up a device by its id.
 *
 * @param id the device unique identifier
 */
public record LookupDeviceByIdCommand(UUID id) {}
