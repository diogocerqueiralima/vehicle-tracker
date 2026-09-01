package com.github.diogocerqueiralima.infrastructure.devices.mappers

import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.infrastructure.devices.entities.DeviceEntity
import java.util.UUID

/**
 * Extension function to convert a [DeviceEntity] infrastructure entity to a [Device] domain model.
 */
fun DeviceEntity.toDomain() = Device(
    id = UUID.fromString(this.id),
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    serialNumber = this.serialNumber,
    model = this.model,
    manufacturer = this.manufacturer,
    imei = this.imei
)
