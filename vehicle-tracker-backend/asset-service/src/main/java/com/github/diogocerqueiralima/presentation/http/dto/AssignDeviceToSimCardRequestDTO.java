package com.github.diogocerqueiralima.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Request DTO used to assign a device to a SIM card.
 */
@Schema(description = "Request payload for assigning a device to a SIM card.")
public record AssignDeviceToSimCardRequestDTO(

        @Schema(description = "Unique identifier of the device to assign.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty("device_id")
        UUID deviceId

) {}
