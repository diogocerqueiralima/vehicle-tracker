package com.github.diogocerqueiralima.application.commands;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Command payload used by the presentation layer to request a page of devices.
 */
public record GetDevicePageCommand(

        @Min(value = 1, message = "pageNumber must be greater than or equal to 1")
        int pageNumber,

        @Min(value = 1, message = "pageSize must be greater than zero")
        @Max(value = 50, message = "pageSize must be less than or equal to 50")
        int pageSize

) {}

