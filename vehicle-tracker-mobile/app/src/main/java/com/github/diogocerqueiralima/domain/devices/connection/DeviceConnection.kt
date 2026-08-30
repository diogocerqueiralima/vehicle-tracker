package com.github.diogocerqueiralima.domain.devices.connection

import java.util.UUID

/**
 * Interface for operations related to device connections.
 */
interface DeviceConnection {

    /**
     * Connects to a device with the given [address].
     *
     * @param address The address of the device to connect to.
     */
    suspend fun connect(address: String)

    /**
     * Reads the value of the characteristic identified by [characteristicId], within the
     * service identified by [serviceId].
     *
     * @param serviceId The id of the service that contains the characteristic.
     * @param characteristicId The id of the characteristic to read.
     * @return The value currently held by the characteristic.
     */
    suspend fun read(serviceId: UUID, characteristicId: UUID): ByteArray

    /**
     * Writes [value] to the characteristic identified by [characteristicId], within the
     * service identified by [serviceId].
     *
     * @param serviceId The id of the service that contains the characteristic.
     * @param characteristicId The id of the characteristic to write to.
     * @param value The value to write.
     */
    suspend fun write(serviceId: UUID, characteristicId: UUID, value: ByteArray)

    /**
     * Releases any resources held by this connection.
     */
    fun close()

}
