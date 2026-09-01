package com.github.diogocerqueiralima.infrastructure.vehicles.mappers

import com.github.diogocerqueiralima.domain.vehicles.model.Vehicle
import com.github.diogocerqueiralima.infrastructure.vehicles.entities.VehicleEntity
import java.time.LocalDate

/**
 * Extension function to convert a [VehicleEntity] infrastructure entity to a [Vehicle] domain model.
 */
fun VehicleEntity.toDomain() = Vehicle(
    vin = this.vin,
    plate = this.plate,
    model = this.model,
    manufacturer = this.manufacturer,
    manufacturingDate = LocalDate.parse(this.manufacturingDate)
)