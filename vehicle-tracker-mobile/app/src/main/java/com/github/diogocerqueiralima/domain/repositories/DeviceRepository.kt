package com.github.diogocerqueiralima.domain.repositories

import com.github.diogocerqueiralima.domain.model.Device
import java.util.UUID

/**
 * Repository interface for managing Device entities.
 */
interface DeviceRepository {

    /**
     * Finds a Device by its unique identifier.
     *
     * @param id The unique identifier of the Device.
     * @return The Device if found, or null if no Device with the given ID exists.
     */
    suspend fun findById(id: UUID): Device?

    /**
     * @return A list of all Devices available in the repository.
     */
    suspend fun findAll(): List<Device>

    /**
     * Creates a new Device owned by the given user.
     *
     * @param serialNumber Manufacturer-assigned serial number of the device.
     * @param model Model name of the device.
     * @param manufacturer Manufacturer of the device.
     * @param imei International Mobile Equipment Identity of the device.
     * @param ownerId Identifier of the user who owns the device.
     * @return The created Device.
     */
    suspend fun create(serialNumber: String, model: String, manufacturer: String, imei: String, ownerId: UUID): Device

}
