package com.github.diogocerqueiralima.application.results;

import java.util.UUID;

public record VehicleResult(
        UUID id, String vin, String plate, String model, String manufacturer, Integer year, UUID ownerId
) {}