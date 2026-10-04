package com.github.diogocerqueiralima.asset.service.application.results

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Result returned by vehicle application use cases.
 */
data class VehicleResult(
    val id: UUID,
    val createdAt: Instant,
    val updatedAt: Instant,
    val vin: String,
    val plate: String,
    val model: String,
    val manufacturer: String,
    val manufacturingDate: LocalDate
)
