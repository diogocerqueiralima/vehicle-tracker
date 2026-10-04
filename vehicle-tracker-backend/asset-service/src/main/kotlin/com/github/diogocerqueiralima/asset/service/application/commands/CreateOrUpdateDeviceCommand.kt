package com.github.diogocerqueiralima.asset.service.application.commands

import jakarta.validation.constraints.NotBlank
import java.util.UUID

/**
 * Command payload used by the presentation layer to request device creation or update.
 */
data class CreateOrUpdateDeviceCommand(
    val id: UUID,
    @get:NotBlank(message = "serialNumber is required") val serialNumber: String,
    @get:NotBlank(message = "model is required") val model: String,
    @get:NotBlank(message = "manufacturer is required") val manufacturer: String,
    @get:NotBlank(message = "imei is required") val imei: String,
    val ownerId: UUID
)
