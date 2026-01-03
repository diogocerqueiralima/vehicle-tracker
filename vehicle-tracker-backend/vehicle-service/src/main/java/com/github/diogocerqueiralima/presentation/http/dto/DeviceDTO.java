package com.github.diogocerqueiralima.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record DeviceDTO(
        UUID id, String imei, @JsonProperty("serial_number") String serialNumber,
        String manufacturer, @JsonProperty("vehicle_id") UUID vehicleId
) {}
