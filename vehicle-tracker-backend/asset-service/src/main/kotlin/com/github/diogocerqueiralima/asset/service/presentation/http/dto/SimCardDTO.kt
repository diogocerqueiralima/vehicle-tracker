package com.github.diogocerqueiralima.asset.service.presentation.http.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "SIM card information returned by the API.")
data class SimCardDTO(
    @field:Schema(description = "Unique identifier of the SIM card.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("id")
    val id: UUID,
    @field:Schema(description = "Timestamp when the SIM card was created.", example = "2024-01-15T10:30:00Z")
    @JsonProperty("created_at")
    val createdAt: Instant,
    @field:Schema(description = "Timestamp when the SIM card was last updated.", example = "2024-06-01T08:00:00Z")
    @JsonProperty("updated_at")
    val updatedAt: Instant,
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
