package com.github.diogocerqueiralima.asset.service.infrastructure.mappers

import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.DeviceEntity

object DeviceMapper {

    fun toEntity(device: Device): DeviceEntity =
        DeviceEntity().apply {
            id = device.id
            ownerId = device.ownerId
            createdAt = device.createdAt
            updatedAt = device.updatedAt
            serialNumber = device.serialNumber
            model = device.model
            manufacturer = device.manufacturer
            imei = device.imei
        }

    fun toDomain(entity: DeviceEntity): Device =
        Device(
            entity.id,
            entity.ownerId,
            entity.createdAt,
            entity.updatedAt,
            entity.serialNumber,
            entity.model,
            entity.manufacturer,
            entity.imei
        )

}
