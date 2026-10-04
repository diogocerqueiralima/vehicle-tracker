package com.github.diogocerqueiralima.asset.service.location

import java.time.Instant
import java.util.UUID

data class ReceiveLocationEvent(
    val timestamp: Instant,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Double,
    val course: Double,
    val deviceId: UUID
)
