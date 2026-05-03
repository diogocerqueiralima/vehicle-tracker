package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotBlank;

/**
 * Command payload used by the presentation layer to request SIM card update.
 */
public record UpdateSimCardCommand(

        @NotBlank(message = "iccid is required")
        String iccid,

        @NotBlank(message = "msisdn is required")
        String msisdn,

        @NotBlank(message = "imsi is required")
        String imsi

) {}

