@file:OptIn(ExperimentalUuidApi::class)

package com.github.diogocerqueiralima.domain.devices.connection

import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Interface for operations related to device connections.
 */
interface DeviceConnection {

    /**
     * Scans for the device with the given [id] and connects to it once found.
     *
     * @param id The id of the device to connect to.
     */
    suspend fun connect(id: UUID)

    /**
     * Reads the value of the characteristic identified by [characteristicId], within the
     * service identified by [serviceId].
     *
     * @param serviceId The id of the service that contains the characteristic.
     * @param characteristicId The id of the characteristic to read.
     * @return The value currently held by the characteristic.
     */
    suspend fun read(serviceId: Uuid, characteristicId: Uuid): ByteArray

    /**
     * Writes [value] to the characteristic identified by [characteristicId], within the
     * service identified by [serviceId].
     *
     * @param serviceId The id of the service that contains the characteristic.
     * @param characteristicId The id of the characteristic to write to.
     * @param value The value to write.
     */
    suspend fun write(serviceId: Uuid, characteristicId: Uuid, value: ByteArray)

    /**
     * Releases any resources held by this connection.
     */
    fun close()

}
