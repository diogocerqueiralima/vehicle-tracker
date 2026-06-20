package com.github.diogocerqueiralima.presentation.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.diogocerqueiralima.domain.assignments.SimCardRemovalReason;

import java.util.UUID;

/**
 * Request DTO used to unassign a device from a SIM card.
 */
public record UnassignDeviceFromSimCardRequestDTO(

        @JsonProperty("device_id")
        UUID deviceId,

        @JsonProperty("removal_reason")
        SimCardRemovalReason removalReason

) {}

