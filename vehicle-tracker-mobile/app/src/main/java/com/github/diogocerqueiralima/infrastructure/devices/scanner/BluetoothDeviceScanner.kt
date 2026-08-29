package com.github.diogocerqueiralima.infrastructure.devices.scanner

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import androidx.annotation.RequiresPermission
import com.github.diogocerqueiralima.domain.common.exceptions.InternalErrorException
import com.github.diogocerqueiralima.domain.devices.model.ScannedDevice
import com.github.diogocerqueiralima.domain.devices.scanner.DeviceScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.nio.ByteBuffer
import java.util.UUID

/**
 * Implementation of [DeviceScanner] that scans Bluetooth devices.
 */
class BluetoothDeviceScanner(private val bluetoothScanner: BluetoothLeScanner) : DeviceScanner {

    constructor(bluetoothManager: BluetoothManager) : this(bluetoothManager.adapter.bluetoothLeScanner)

    private companion object {

        private const val MANUFACTURER_ID = 0xFFFF

    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override suspend fun scan(deviceId: UUID): Flow<ScannedDevice> = callbackFlow {

        // 1. Create a ScanFilter to filter devices by manufacturer data (using the deviceId as the manufacturer data).
        val filters = listOf<ScanFilter>(
            ScanFilter.Builder()
                .setManufacturerData(MANUFACTURER_ID, deviceId.toByteArray())
                .build()
        )

        // 2. Create ScanSettings to configure the scanning behavior (e.g., scan mode, callback type).
        // - SCAN_MODE_LOW_LATENCY: Scans for devices with low latency, which is suitable for real-time applications.
        // - CALLBACK_TYPE_ALL_MATCHES: Receives all matching scan results, not just the first one.
        // - report delay 0: Reports scan results immediately without batching.
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setReportDelay(0L)
            .build()

        // 3. Create a ScanCallback to handle scan results and errors.
        val callback = object : ScanCallback() {

            override fun onScanResult(callbackType: Int, result: ScanResult) {

                val scannedDevice = result.toScannedDevice(deviceId)
                trySend(scannedDevice)

            }

            override fun onScanFailed(errorCode: Int) {
                close(InternalErrorException("Scan failed with error code: $errorCode"))
            }

        }

        // 4. Start scanning for devices using the BluetoothLeScanner with the specified filters and settings.
        bluetoothScanner.startScan(filters, settings, callback)
    }

    private fun UUID.toByteArray(): ByteArray {

        val buffer = ByteBuffer.allocate(16)

        buffer.putLong(this.mostSignificantBits)
        buffer.putLong(this.leastSignificantBits)

        return buffer.array()
    }

    private fun ScanResult.toScannedDevice(id: UUID): ScannedDevice = ScannedDevice(
        id = id,
        address = this.device.address,
        rssi = this.rssi
    )

}