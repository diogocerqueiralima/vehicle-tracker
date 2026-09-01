package com.github.diogocerqueiralima.domain.vehicles.model

import java.time.LocalDate

/**
 * Represents a vehicle asset with its identifying and manufacturing attributes.
 */
data class Vehicle(
    val vin: String,
    val plate: String,
    val model: String,
    val manufacturer: String,
    val manufacturingDate: LocalDate
) {

    init {

        // 1. VIN must follow the standard 17-character format.
        require(vin.isNotBlank()) { "vin must not be blank" }
        require(vin.length == 17) { "vin must be 17 characters long" }

        // 2. Plate, model and manufacturer are required identifying attributes.
        require(plate.isNotBlank()) { "plate must not be blank" }
        require(model.isNotBlank()) { "model must not be blank" }
        require(manufacturer.isNotBlank()) { "manufacturer must not be blank" }

        // 3. A vehicle cannot have been manufactured in the future.
        require(!manufacturingDate.isAfter(LocalDate.now())) { "manufacturingDate must not be in the future" }

    }

    /**
     * Human-readable name composed of the manufacturer and model.
     */
    val displayName = "$manufacturer $model"

}