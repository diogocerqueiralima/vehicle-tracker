package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Device payload returned by presentation endpoints.
 */
public record DeviceDTO(
        @JsonProperty("id") UUID id,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("serial_number") String serialNumber,
        @JsonProperty("model") String model,
        @JsonProperty("manufacturer") String manufacturer,
        @JsonProperty("imei") String imei
) {}