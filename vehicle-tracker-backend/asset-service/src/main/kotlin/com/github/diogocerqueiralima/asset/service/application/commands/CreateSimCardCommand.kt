package com.github.diogocerqueiralima.asset.service.application.commands

import jakarta.validation.constraints.NotBlank
import java.util.UUID

/**
 * Command payload used by the presentation layer to request SIM card creation.
 */
data class CreateSimCardCommand(
    @get:NotBlank(message = "iccid is required") val iccid: String,
    @get:NotBlank(message = "msisdn is required") val msisdn: String,
    @get:NotBlank(message = "imsi is required") val imsi: String,
    val userId: UUID
)
