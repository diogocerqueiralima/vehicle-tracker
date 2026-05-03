package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SIM card payload returned by presentation endpoints.
 */
public record SimCardDTO(
        @JsonProperty("iccid") String iccid,
        @JsonProperty("msisdn") String msisdn,
        @JsonProperty("imsi") String imsi
) {}

