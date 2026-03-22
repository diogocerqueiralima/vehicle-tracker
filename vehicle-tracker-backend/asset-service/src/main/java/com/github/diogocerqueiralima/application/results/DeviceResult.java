package com.github.diogocerqueiralima.application.results;

import java.time.Instant;
import java.util.UUID;

/**
 * Result returned by device application use cases.
 */
public record DeviceResult(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String serialNumber,
        String model,
        String manufacturer,
        String imei
) {}

