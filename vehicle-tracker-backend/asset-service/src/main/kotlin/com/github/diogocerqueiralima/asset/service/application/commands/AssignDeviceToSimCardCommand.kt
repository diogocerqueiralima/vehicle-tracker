package com.github.diogocerqueiralima.asset.service.application.commands

import java.util.UUID

/**
 * Command payload used by the presentation layer to request assigning a device to a SIM card.
 */
data class AssignDeviceToSimCardCommand(
    val deviceId: UUID,
    val simCardId: UUID,
    val assignedBy: UUID
)
