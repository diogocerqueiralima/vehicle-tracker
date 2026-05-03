package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for SIM card update.
 */
public record UpdateSimCardRequestDTO(

        @JsonProperty("iccid")
        String iccid,

        @JsonProperty("msisdn")
        String msisdn,

        @JsonProperty("imsi")
        String imsi

) {}

