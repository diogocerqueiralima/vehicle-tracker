package com.github.diogocerqueiralima.ingestion.service.domain.model

import java.time.Instant
import java.util.UUID

data class Location(
    val timestamp: Instant,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Double,
    val course: Double,
    val deviceId: UUID
)
