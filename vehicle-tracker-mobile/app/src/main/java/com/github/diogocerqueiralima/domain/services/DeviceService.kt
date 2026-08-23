package com.github.diogocerqueiralima.domain.services

import com.github.diogocerqueiralima.domain.exceptions.DeviceNotFoundException
import com.github.diogocerqueiralima.domain.model.Device
import com.github.diogocerqueiralima.domain.repositories.DeviceRepository
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
     * Creates a new device owned by the given user.
     */
    suspend fun create(serialNumber: String, model: String, manufacturer: String, imei: String, ownerId: UUID): Device =
        deviceRepository.create(serialNumber, model, manufacturer, imei, ownerId)

    /**
     * Retrieves a device by its unique identifier.
     *
     * @param id The unique identifier of the Device.
     * @throws DeviceNotFoundException if no Device with the given id exists.
     */
    suspend fun findById(id: UUID): Device =
        deviceRepository.findById(id) ?: throw DeviceNotFoundException(id)

}
