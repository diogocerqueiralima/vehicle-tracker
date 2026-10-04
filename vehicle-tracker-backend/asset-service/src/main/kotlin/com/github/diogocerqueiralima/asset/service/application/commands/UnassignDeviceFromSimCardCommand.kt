package com.github.diogocerqueiralima.asset.service.application.commands

import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardRemovalReason
import java.util.UUID

/**
 * Command payload used by the presentation layer to request unassigning a device from a SIM card.
 */
data class UnassignDeviceFromSimCardCommand(
    val deviceId: UUID,
    val simCardId: UUID,
    val unassignedBy: UUID,
    val removalReason: SimCardRemovalReason?
)
