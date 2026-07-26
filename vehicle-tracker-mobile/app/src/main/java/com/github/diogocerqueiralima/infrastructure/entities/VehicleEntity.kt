package com.github.diogocerqueiralima.infrastructure.entities

import java.util.UUID

/**
 * Data class representing a vehicle entity with an ID, name, and plate number.
 */
data class VehicleEntity(

    val id: UUID,
    val name: String,
    val plate: String

)
