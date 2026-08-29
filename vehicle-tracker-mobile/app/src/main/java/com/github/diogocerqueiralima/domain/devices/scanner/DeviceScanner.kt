package com.github.diogocerqueiralima.domain.devices.scanner

import com.github.diogocerqueiralima.domain.devices.model.ScannedDevice
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Represents a scanner that can scan devices.
 */
interface DeviceScanner {

    /**
     * Scans a device with the given [deviceId].
     */
    suspend fun scan(deviceId: UUID): Flow<ScannedDevice>

}