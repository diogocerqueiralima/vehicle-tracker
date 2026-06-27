package com.github.diogocerqueiralima.asset.service.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request DTO for SIM card creation.
 */
@Schema(description = "Request payload for creating a new SIM card.")
public record CreateSimCardRequestDTO(

        @Schema(description = "Integrated Circuit Card Identifier (ICCID) of the SIM card.", example = "8955100000000000001")
        @JsonProperty("iccid")
        String iccid,

        @Schema(description = "Mobile Station International Subscriber Directory Number (MSISDN).", example = "+351912345678")
        @JsonProperty("msisdn")
        String msisdn,

        @Schema(description = "International Mobile Subscriber Identity (IMSI) of the SIM card.", example = "268010000000001")
        @JsonProperty("imsi")
        String imsi

) {}
