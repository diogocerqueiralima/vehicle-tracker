package com.github.diogocerqueiralima.presentation.http.dto;

import java.util.UUID;

public record VehicleDTO(
        UUID id, String vin, String plate, String model, String manufacturer, Integer year, UUID ownerId
) {}
