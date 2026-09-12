@file:OptIn(ExperimentalUuidApi::class)

package com.github.diogocerqueiralima.domain.devices.services

import android.util.Log
import com.github.diogocerqueiralima.domain.common.repositories.FileRepository
import com.github.diogocerqueiralima.domain.devices.catalog.CharacteristicFormat
import com.github.diogocerqueiralima.domain.devices.catalog.CharacteristicSpec
import com.github.diogocerqueiralima.domain.devices.connection.DeviceConnection
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "DEVICE_CONFIGURATION_SERVICE"

/**
 * Service responsible for connecting to and configuring devices.
 */
class DeviceConfigurationService(
    private val deviceConnection: DeviceConnection,
    private val fileRepository: FileRepository
) {

    private companion object {

        // Every value the device hands out as a file is a PEM-encoded credential.
        const val FILE_EXTENSION = "pem"
        const val FILE_MIME_TYPE = "application/x-pem-file"

    }

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
     * Reads [characteristic] and saves its value to the user's downloads, as the bytes the device
     * sent them.
     *
     * @param characteristic The characteristic to read, whose name the file is named after. Must
     * be a [CharacteristicFormat.FILE] one, the only kind the app hands over as a file instead of
     * displaying.
     * @return The name the file was saved under.
     * @throws IllegalArgumentException if [characteristic] is not a [CharacteristicFormat.FILE] one.
     */
    suspend fun download(characteristic: CharacteristicSpec): String {

        require(characteristic.format == CharacteristicFormat.FILE) {
            "Characteristic ${characteristic.name} is not a file, and is read as a value instead"
        }

        Log.d(TAG, "Downloading characteristic: ${characteristic.key}")

        val value = deviceConnection.read(characteristic.serviceId, characteristic.characteristicId)

        return fileRepository.download("${characteristic.name}.$FILE_EXTENSION", FILE_MIME_TYPE, value)
    }

    /**
     * Releases any resources held by the underlying device connection.
     */
    fun disconnect() {
        deviceConnection.close()
    }

}
