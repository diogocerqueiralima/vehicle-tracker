package com.github.diogocerqueiralima.asset.service.application.commands

import com.github.diogocerqueiralima.asset.service.application.validation.Plate
import com.github.diogocerqueiralima.asset.service.application.validation.VIN
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate
import java.util.UUID

/**
 * Command payload used by the presentation layer to request vehicle creation.
 */
data class CreateVehicleCommand(
    @get:NotBlank(message = "vin is required") @get:VIN val vin: String,
    @get:NotBlank(message = "plate is required") @get:Plate val plate: String,
    @get:NotBlank(message = "model is required") val model: String,
    @get:NotBlank(message = "manufacturer is required") val manufacturer: String,
    @get:PastOrPresent(message = "manufacturingDate cannot be in the future") val manufacturingDate: LocalDate,
    val userId: UUID
)
