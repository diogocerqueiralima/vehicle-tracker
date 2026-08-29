package com.github.diogocerqueiralima.domain.devices.services

import com.github.diogocerqueiralima.domain.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.domain.devices.repositories.DeviceRepository
import java.util.UUID

/**
 * Service responsible for handling device-related use cases.
 */
class DeviceService(
    private val deviceRepository: DeviceRepository
) {

    /**
     * Retrieves all devices.
     */
    suspend fun findAll(): List<Device> = deviceRepository.findAll()

    /**
     * Creates or updates a device identified by [id], owned by the given user.
     */
    suspend fun createOrUpdate(id: UUID, serialNumber: String, model: String, manufacturer: String, imei: String, ownerId: UUID): Device =
        deviceRepository.createOrUpdate(id, serialNumber, model, manufacturer, imei, ownerId)

    /**
     * Retrieves a device by its unique identifier.
     *
     * @param id The unique identifier of the Device.
     * @throws NotFoundException if no Device with the given id exists.
     */
    suspend fun findById(id: UUID): Device =
        deviceRepository.findById(id) ?: throw NotFoundException(id)

}
