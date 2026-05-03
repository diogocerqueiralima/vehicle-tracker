package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Request DTO for device update.
 */
public record UpdateDeviceRequestDTO(

        @JsonProperty("serial_number")
        String serialNumber,

        @JsonProperty("model")
        String model,

        @JsonProperty("manufacturer")
        String manufacturer,

        @JsonProperty("imei")
        String imei,

        @JsonProperty("owner_id")
        UUID ownerId

) {}

