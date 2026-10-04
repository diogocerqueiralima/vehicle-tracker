package com.github.diogocerqueiralima.asset.service.infrastructure.mappers

import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.VehicleEntity

object VehicleMapper {

    fun toEntity(vehicle: Vehicle): VehicleEntity =
        VehicleEntity().apply {
            id = vehicle.id
            ownerId = vehicle.ownerId
            createdAt = vehicle.createdAt
            updatedAt = vehicle.updatedAt
            vin = vehicle.vin
            plate = vehicle.plate
            model = vehicle.model
            manufacturer = vehicle.manufacturer
            manufacturingDate = vehicle.manufacturingDate
        }

    fun toDomain(entity: VehicleEntity): Vehicle =
        Vehicle(
            entity.id,
            entity.ownerId,
            entity.createdAt,
            entity.updatedAt,
            entity.vin,
            entity.plate,
            entity.model,
            entity.manufacturer,
            entity.manufacturingDate
        )

}
