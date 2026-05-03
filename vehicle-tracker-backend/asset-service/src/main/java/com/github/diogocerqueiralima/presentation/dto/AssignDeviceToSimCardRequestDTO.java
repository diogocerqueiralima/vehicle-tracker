package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Request DTO used to assign a device to a SIM card.
 */
public record AssignDeviceToSimCardRequestDTO(

        @JsonProperty("device_id")
        UUID deviceId,

        @JsonProperty("sim_card_id")
        UUID simCardId

) {}

