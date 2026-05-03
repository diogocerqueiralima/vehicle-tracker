package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SIM card payload returned by presentation endpoints.
 */
public record SimCardDTO(
        @JsonProperty("id") java.util.UUID id,
        @JsonProperty("created_at") java.time.Instant createdAt,
        @JsonProperty("updated_at") java.time.Instant updatedAt,
        @JsonProperty("id") String iccid,
        @JsonProperty("msisdn") String msisdn,
        @JsonProperty("imsi") String imsi
) {}

