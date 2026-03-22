package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for device creation.
 */
public record CreateDeviceRequestDTO(

        @NotBlank(message = "serialNumber is required")
        @JsonProperty("serial_number")
        String serialNumber,

        @NotBlank(message = "model is required")
        @JsonProperty("model")
        String model,

        @NotBlank(message = "manufacturer is required")
        @JsonProperty("manufacturer")
        String manufacturer,

        @NotBlank(message = "imei is required")
        @JsonProperty("imei")
        String imei

) {}

