package com.github.diogocerqueiralima.asset.service.application.results

import java.time.Instant
import java.util.UUID

/**
 * Result returned by device application use cases.
 */
data class DeviceResult(
    val id: UUID,
    val ownerId: UUID?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val serialNumber: String,
    val model: String,
    val manufacturer: String,
    val imei: String
)
