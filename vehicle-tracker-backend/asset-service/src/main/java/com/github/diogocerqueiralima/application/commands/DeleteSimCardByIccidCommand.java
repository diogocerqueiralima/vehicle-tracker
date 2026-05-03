package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.NotBlank;

/**
 * Command payload used by the presentation layer to request SIM card deletion by ICCID.
 */
public record DeleteSimCardByIccidCommand(

        @NotBlank(message = "iccid is required")
        String iccid

) {}

