package com.github.diogocerqueiralima.asset.service.application.commands;

import java.util.UUID;

/**
 * Command to check if a device is owned by a specific user.
 */
public record DeviceIsOwnedByUserCommand(UUID deviceId, UUID userId) {}
