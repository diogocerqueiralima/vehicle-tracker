package com.github.diogocerqueiralima.asset.service.application.commands

import java.util.UUID

/**
 * Command payload used by the presentation layer to request assigning a device to a vehicle.
 */
data class AssignDeviceToVehicleCommand(
    val deviceId: UUID,
    val vehicleId: UUID,
    val assignedBy: UUID,
    val installedBy: UUID?,
    val notes: String?
)
