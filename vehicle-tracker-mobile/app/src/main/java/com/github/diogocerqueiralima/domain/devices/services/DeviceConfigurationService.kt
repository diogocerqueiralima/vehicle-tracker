@file:OptIn(ExperimentalUuidApi::class)

package com.github.diogocerqueiralima.domain.devices.services

import android.util.Log
import com.github.diogocerqueiralima.domain.devices.connection.DeviceConnection
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "DEVICE_CONFIGURATION_SERVICE"

/**
 * Service responsible for connecting to and configuring devices.
 */
class DeviceConfigurationService(
    private val deviceConnection: DeviceConnection
) {

    /**
     * Scans for the device with the given [id] and connects to it once found.
     *
     * @throws com.github.diogocerqueiralima.domain.common.exceptions.NotFoundException if the device with the given [id] is not found.
     */
    suspend fun connect(id: UUID) {
        Log.d(TAG, "Attempting to connect to device: $id")
        deviceConnection.connect(id)
    }

    /**
     * Reads the value of the characteristic identified by [characteristicId], within the
     * service identified by [serviceId].
     */
    suspend fun read(serviceId: Uuid, characteristicId: Uuid): ByteArray =
        deviceConnection.read(serviceId, characteristicId)

    /**
     * Writes [value] to the characteristic identified by [characteristicId], within the
     * service identified by [serviceId].
     */
    suspend fun write(serviceId: Uuid, characteristicId: Uuid, value: ByteArray) {
        deviceConnection.write(serviceId, characteristicId, value)
    }

    /**
     * Releases any resources held by the underlying device connection.
     */
    fun disconnect() {
        deviceConnection.close()
    }

}
