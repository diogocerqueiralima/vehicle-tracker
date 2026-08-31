package com.github.diogocerqueiralima.domain.devices.services

import android.util.Log
import com.github.diogocerqueiralima.domain.devices.connection.DeviceConnection
import com.github.diogocerqueiralima.domain.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.domain.devices.scanner.DeviceScanner
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.timeout
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

private const val TAG = "DEVICE_CONFIGURATION_SERVICE"

/**
 * Service responsible for scanning and connecting to devices.
 */
class DeviceConfigurationService(
    private val deviceScanner: DeviceScanner,
    private val deviceConnection: DeviceConnection
) {

    /**
     * Scans for the device with the given [id] and connects to it once found.
     * The scan will time out after 30 seconds if the device is not found.
     *
     * @throws NotFoundException if the device is not found within the timeout period.
     */
    @OptIn(FlowPreview::class)
    suspend fun connect(id: UUID) {

        Log.d(TAG, "Starting connection to device: $id")

        val scannedDevice = deviceScanner.scan(id)
            .timeout(30.seconds)
            .catch {
                Log.w(TAG, "Device not found within timeout: $id")
                throw NotFoundException(id)
            }
            .firstOrNull() ?: run {
            Log.w(TAG, "Device not found: $id")
            throw NotFoundException(id)
        }

        Log.d(TAG, "Device found: ${scannedDevice.address}. Attempting to connect...")

        deviceConnection.connect(scannedDevice.address)
    }

    /**
     * Reads the value of the characteristic identified by [characteristicId], within the
     * service identified by [serviceId].
     */
    suspend fun read(serviceId: UUID, characteristicId: UUID): ByteArray =
        deviceConnection.read(serviceId, characteristicId)

    /**
     * Releases any resources held by the underlying device connection.
     */
    fun disconnect() {
        deviceConnection.close()
    }

}
