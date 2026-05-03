package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for SIM card creation.
 */
public record CreateSimCardRequestDTO(

        @JsonProperty("iccid")
        String iccid,

        @JsonProperty("msisdn")
        String msisdn,

        @JsonProperty("imsi")
        String imsi

) {}

