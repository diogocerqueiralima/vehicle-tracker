package com.github.diogocerqueiralima.asset.service.presentation.http.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request payload for creating a new SIM card.")
data class CreateSimCardRequestDTO(
    @field:Schema(description = "Integrated Circuit Card Identifier (ICCID) of the SIM card.", example = "8955100000000000001")
    @JsonProperty("iccid")
    val iccid: String,
    @field:Schema(description = "Mobile Station International Subscriber Directory Number (MSISDN).", example = "+351912345678")
    @JsonProperty("msisdn")
    val msisdn: String,
    @field:Schema(description = "International Mobile Subscriber Identity (IMSI) of the SIM card.", example = "268010000000001")
    @JsonProperty("imsi")
    val imsi: String
)
