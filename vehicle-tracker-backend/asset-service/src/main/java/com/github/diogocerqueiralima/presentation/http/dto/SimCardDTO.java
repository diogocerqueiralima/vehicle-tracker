package com.github.diogocerqueiralima.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * SIM card payload returned by presentation endpoints.
 */
@Schema(description = "SIM card information returned by the API.")
public record SimCardDTO(

        @Schema(description = "Unique identifier of the SIM card.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty("id") UUID id,

        @Schema(description = "Timestamp when the SIM card was created.", example = "2024-01-15T10:30:00Z")
        @JsonProperty("created_at") Instant createdAt,

        @Schema(description = "Timestamp when the SIM card was last updated.", example = "2024-06-01T08:00:00Z")
        @JsonProperty("updated_at") Instant updatedAt,

        @Schema(description = "Integrated Circuit Card Identifier (ICCID) of the SIM card.", example = "8955100000000000001")
        @JsonProperty("iccid") String iccid,

        @Schema(description = "Mobile Station International Subscriber Directory Number (MSISDN).", example = "+351912345678")
        @JsonProperty("msisdn") String msisdn,

        @Schema(description = "International Mobile Subscriber Identity (IMSI) of the SIM card.", example = "268010000000001")
        @JsonProperty("imsi") String imsi

) {}
