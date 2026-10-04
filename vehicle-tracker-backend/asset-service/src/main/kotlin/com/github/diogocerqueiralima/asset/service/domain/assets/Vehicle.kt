package com.github.diogocerqueiralima.asset.service.domain.assets

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Represents a vehicle asset in the system.
 * A vehicle has a VIN, plate number, model, manufacturer, and manufacturing date.
 */
class Vehicle(
    id: UUID,
    ownerId: UUID?,
    createdAt: Instant,
    updatedAt: Instant,
    val vin: String,
    val plate: String,
    val model: String,
    val manufacturer: String,
    val manufacturingDate: LocalDate
) : Asset(id, ownerId, createdAt, updatedAt) {

    constructor(
        id: UUID, createdAt: Instant, updatedAt: Instant, vin: String, plate: String,
        model: String, manufacturer: String, manufacturingDate: LocalDate
    ) : this(id, null, createdAt, updatedAt, vin, plate, model, manufacturer, manufacturingDate)

}
