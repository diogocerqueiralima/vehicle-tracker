package com.github.diogocerqueiralima.infrastructure.entities

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a device entity as received from the Asset Service.
 */
@Serializable
data class DeviceEntity(

    val id: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    @SerialName("serial_number") val serialNumber: String,
    val model: String,
    val manufacturer: String,
    val imei: String

)
