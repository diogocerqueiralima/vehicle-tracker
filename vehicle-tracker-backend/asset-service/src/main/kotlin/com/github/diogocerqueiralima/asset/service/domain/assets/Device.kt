package com.github.diogocerqueiralima.asset.service.domain.assets

import java.time.Instant
import java.util.UUID

/**
 * Represents a device asset in the system.
 * The device is the physical hardware that can be assigned to a vehicle and have a SIM card installed.
 * It is used to track the [Vehicle]
 */
class Device(
    id: UUID,
    ownerId: UUID?,
    createdAt: Instant,
    updatedAt: Instant,
    val serialNumber: String,
    val model: String,
    val manufacturer: String,
    val imei: String
) : Asset(id, ownerId, createdAt, updatedAt) {

    constructor(
        id: UUID, createdAt: Instant, updatedAt: Instant, serialNumber: String, model: String,
        manufacturer: String, imei: String
    ) : this(id, null, createdAt, updatedAt, serialNumber, model, manufacturer, imei)

}
