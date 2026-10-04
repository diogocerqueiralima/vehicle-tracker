package com.github.diogocerqueiralima.asset.service.application.commands

import java.util.UUID

/**
 * Command payload used by the presentation layer to request SIM card retrieval by id.
 */
data class GetSimCardByIdCommand(
    val id: UUID,
    val userId: UUID
)
