package com.github.diogocerqueiralima.infrastructure.vehicles.entities

import kotlinx.serialization.SerialName
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
    @SerialName("manufacturing_date") val manufacturingDate: String

)
