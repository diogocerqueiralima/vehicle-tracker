package com.github.diogocerqueiralima.domain.model

import java.util.UUID
import kotlin.time.Instant

/**
 * Represents a tracking device asset with its identifying and manufacturing attributes.
 */
data class Device(
    val id: UUID,
    val createdAt: Instant,
    val updatedAt: Instant,
    val serialNumber: String,
    val model: String,
    val manufacturer: String,
    val imei: String
) {

    init {

        // 1. Serial number, model and manufacturer are required identifying attributes.
        require(serialNumber.isNotBlank()) { "serialNumber must not be blank" }
        require(model.isNotBlank()) { "model must not be blank" }
        require(manufacturer.isNotBlank()) { "manufacturer must not be blank" }

        // 2. IMEI must follow the standard 15-digit format.
        require(imei.isNotBlank()) { "imei must not be blank" }
        require(imei.length == 15) { "imei must be 15 characters long" }

    }

    /**
     * Human-readable name composed of the manufacturer and model.
     */
    val displayName = "$manufacturer $model"

}
