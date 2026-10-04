package com.github.diogocerqueiralima.asset.service.application.commands

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.util.UUID

/**
 * Command payload used by the presentation layer to request a page of vehicles.
 */
data class GetVehiclePageCommand(
    @get:Min(value = 1, message = "pageNumber must be greater than or equal to 1") val pageNumber: Int,
    @get:Min(value = 1, message = "pageSize must be greater than zero") @get:Max(value = 50, message = "pageSize must be less than or equal to 50") val pageSize: Int,
    val userId: UUID
)
