package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO used to assign a device to a SIM card.
 */
public record AssignDeviceToSimCardRequestDTO(

        @NotNull(message = "deviceId is required")
        @JsonProperty("device_id")
        UUID deviceId,

        @NotNull(message = "simCardId is required")
        @JsonProperty("sim_card_id")
        UUID simCardId

) {}

