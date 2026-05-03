package com.github.diogocerqueiralima.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.diogocerqueiralima.domain.assignments.SimCardRemovalReason;

import java.util.UUID;

/**
 * Request DTO used to unassign a device from a SIM card.
 */
public record UnassignDeviceFromSimCardRequestDTO(

        @JsonProperty("device_id")
        UUID deviceId,

        @JsonProperty("sim_card_id")
        UUID simCardId,

        @JsonProperty("removal_reason")
        SimCardRemovalReason removalReason

) {}

