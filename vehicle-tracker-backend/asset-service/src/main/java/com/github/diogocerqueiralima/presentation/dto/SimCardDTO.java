package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * SIM card payload returned by presentation endpoints.
 */
public record SimCardDTO(
        @JsonProperty("id") UUID id,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("iccid") String iccid,
        @JsonProperty("msisdn") String msisdn,
        @JsonProperty("imsi") String imsi
) {}

