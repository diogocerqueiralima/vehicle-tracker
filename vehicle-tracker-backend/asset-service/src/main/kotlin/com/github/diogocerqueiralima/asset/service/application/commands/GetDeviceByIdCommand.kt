package com.github.diogocerqueiralima.asset.service.application.commands

import java.util.UUID

/**
 * Command payload used by the presentation layer to request a device by id.
 */
data class GetDeviceByIdCommand(
    val id: UUID,
    val userId: UUID,
    val isAdmin: Boolean
)
