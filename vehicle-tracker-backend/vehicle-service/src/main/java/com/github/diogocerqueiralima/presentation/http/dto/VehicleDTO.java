package com.github.diogocerqueiralima.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record VehicleDTO(
        UUID id, String vin, String plate, String model, String manufacturer,
        Integer year, @JsonProperty("owner_id") UUID ownerId
) {}
