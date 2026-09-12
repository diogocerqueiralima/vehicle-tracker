@file:OptIn(ExperimentalUuidApi::class)

package com.github.diogocerqueiralima.domain.devices.services

import android.util.Log
import com.github.diogocerqueiralima.domain.devices.catalog.CharacteristicSpec
import com.github.diogocerqueiralima.domain.devices.connection.DeviceConnection
import java.io.InputStream
import java.io.OutputStream
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
     * Downloads the current value of [characteristic] (a `FILE`-format characteristic) into [sink],
     * using the chunked transfer protocol every `FILE` characteristic is served with (see
     * [com.github.diogocerqueiralima.domain.devices.connection.DeviceConnection.readFile]).
     *
     * @param characteristic The characteristic to be downloaded.
     * @param sink The output stream to which the characteristic's value will be written.
     */
    suspend fun downloadFile(characteristic: CharacteristicSpec, sink: OutputStream) {
        deviceConnection.readFile(characteristic.serviceId, characteristic.characteristicId, sink)
    }

    /**
     * Uploads [length] bytes read from [source] to [characteristic] (a `FILE`-format
     * characteristic), using the chunked transfer protocol.
     *
     * @param characteristic The characteristic to which the file will be uploaded.
     * @param source The input stream from which the file data will be read.
     * @param length The total length of the file data to be uploaded.
     */
    suspend fun uploadFile(characteristic: CharacteristicSpec, source: InputStream, length: Long) {
        deviceConnection.writeFile(characteristic.serviceId, characteristic.characteristicId, source, length)
    }

    /**
     * Releases any resources held by the underlying device connection.
     */
    fun disconnect() {
        deviceConnection.close()
    }

}
