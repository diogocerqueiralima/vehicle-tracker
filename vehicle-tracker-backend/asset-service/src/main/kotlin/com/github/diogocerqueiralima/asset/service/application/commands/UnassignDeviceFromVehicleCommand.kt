package com.github.diogocerqueiralima.asset.service.application.commands

import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleRemovalReason
import java.util.UUID

/**
 * Command payload used by the presentation layer to request unassigning a device from a vehicle.
 */
data class UnassignDeviceFromVehicleCommand(
    val deviceId: UUID,
    val vehicleId: UUID,
    val unassignedBy: UUID,
    val removalReason: VehicleRemovalReason?
)
