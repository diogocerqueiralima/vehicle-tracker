package com.github.diogocerqueiralima.asset.service.application.commands

import java.util.UUID

/**
 * Command to retrieve a vehicle assignment by device id.
 */
data class GetVehicleAssignmentByDeviceIdCommand(
    val deviceId: UUID
)
