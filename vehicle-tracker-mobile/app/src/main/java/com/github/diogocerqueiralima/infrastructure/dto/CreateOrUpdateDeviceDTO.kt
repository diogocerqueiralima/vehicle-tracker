package com.github.diogocerqueiralima.infrastructure.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing the payload sent to the Asset Service to create or update a device.
 */
@Serializable
data class CreateOrUpdateDeviceDTO(

    @SerialName("serial_number") val serialNumber: String,
    val model: String,
    val manufacturer: String,
    val imei: String,
    @SerialName("owner_id") val ownerId: String

)
