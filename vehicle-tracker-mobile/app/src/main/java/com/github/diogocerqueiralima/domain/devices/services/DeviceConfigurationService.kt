package com.github.diogocerqueiralima.domain.devices.services

import com.github.diogocerqueiralima.domain.devices.connection.DeviceConnection
import com.github.diogocerqueiralima.domain.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.domain.devices.scanner.DeviceScanner
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.timeout
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

/**
 * Service responsible for scanning and connecting to devices.
 */
class DeviceConfigurationService(
    private val deviceScanner: DeviceScanner,
    private val deviceConnection: DeviceConnection
) {

    /**
     * Scans for the device with the given [id] and connects to it once found.
     * The scan will time out after 10 seconds if the device is not found.
     *
     * @throws NotFoundException if the device is not found within the timeout period.
     */
    @OptIn(FlowPreview::class)
    suspend fun connect(id: UUID) {

        val scannedDevice = deviceScanner.scan(id)
            .timeout(10.seconds)
            .firstOrNull() ?: throw NotFoundException(id)

        deviceConnection.connect(scannedDevice.address)
    }

}
