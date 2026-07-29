package com.github.diogocerqueiralima.infrastructure.entities

import kotlinx.serialization.Serializable

/**
 * Data class representing a vehicle entity as received from the Asset Service.
 */
@Serializable
data class VehicleEntity(

    val id: String,
    val vin: String,
    val plate: String,
    val model: String,
    val manufacturer: String,
    val manufacturingDate: String

)
