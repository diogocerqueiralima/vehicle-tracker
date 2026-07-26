package com.github.diogocerqueiralima.infrastructure.mappers

import com.github.diogocerqueiralima.domain.model.Vehicle
import com.github.diogocerqueiralima.infrastructure.entities.VehicleEntity

/**
 * Extension function to convert a [Vehicle] domain model to a [VehicleEntity] infrastructure entity.
 */
fun VehicleEntity.toDomain() = Vehicle(
    name = this.name,
    plate = this.plate
)