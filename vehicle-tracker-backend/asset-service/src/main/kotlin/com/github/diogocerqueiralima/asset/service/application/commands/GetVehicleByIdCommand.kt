package com.github.diogocerqueiralima.asset.service.application.commands

import java.util.UUID

/**
 * Command payload used by the presentation layer to request a vehicle by id.
 */
data class GetVehicleByIdCommand(
    val id: UUID,
    val userId: UUID
)
