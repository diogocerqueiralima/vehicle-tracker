package com.github.diogocerqueiralima.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record CreateDeviceDTO(
        String imei, @JsonProperty("serial_number") String serialNumber, String manufacturer, UUID vehicleId
) {}
