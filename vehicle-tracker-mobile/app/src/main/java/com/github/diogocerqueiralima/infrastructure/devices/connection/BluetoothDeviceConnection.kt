@file:OptIn(ExperimentalUuidApi::class)

package com.github.diogocerqueiralima.infrastructure.devices.connection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.diogocerqueiralima.domain.common.exceptions.InternalErrorException
import com.github.diogocerqueiralima.domain.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.domain.devices.connection.DeviceConnection
import com.juul.kable.Filter
import com.juul.kable.ObsoleteKableApi
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "BLUETOOTH_DEVICE_CONNECTION"

private const val MANUFACTURER_ID = 0xFFFF

/**
 * Implementation of [DeviceConnection] for Bluetooth devices.
 *
 * Scanning, GATT connection, service discovery, and characteristic reads/writes are all
 * delegated to Kable. Bonding is still handled manually, since it must happen before the GATT
 * connection is established and Kable has no bonding API of its own: a [BroadcastReceiver] is
 * registered for the duration of a single bonding wait, then unregistered.
 */
class BluetoothDeviceConnection(
    private val context: Context,
    private val adapter: BluetoothAdapter,
    private val dataStore: DataStore<Preferences>
) : DeviceConnection {

    constructor(context: Context, bluetoothManager: BluetoothManager, dataStore: DataStore<Preferences>) :
        this(context, bluetoothManager.adapter, dataStore)

    private var peripheral: Peripheral? = null

    /**
     * Suspends until a [BluetoothDevice.ACTION_BOND_STATE_CHANGED] broadcast reports a
     * non-[BluetoothDevice.BOND_BONDING] state for the device at [address], returning that state.
     */
    private suspend fun awaitBondState(address: String): Int = suspendCancellableCoroutine { continuation ->

        val receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {

                Log.d(TAG, "Received bond state change broadcast for $address")

                val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)

                if (device == null || device.address != address) {
                    return
                }

                val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)

                Log.d(TAG, "Bond state changed for $address: $state")

                if (state == BluetoothDevice.BOND_BONDING) {
                    return
                }

                context.unregisterReceiver(this)

                if (continuation.isActive) {
                    continuation.resume(state)
                }
            }

        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED // Bluetooth bond state changes are system broadcasts, so this receiver must be exported
        )

        // Only reached if the coroutine is cancelled before onReceive unregisters the receiver itself.
        continuation.invokeOnCancellation { runCatching { context.unregisterReceiver(receiver) } }
    }

    /**
     * Returns the MAC address for the device with the given [id]: the address saved from a
     * previous connection if there is one (already-bonded devices aren't reliably found via a
     * fresh scan), otherwise scans for it by manufacturer data.
     *
     * @throws NotFoundException if the device isn't found within the 30-second scan timeout.
     */
    @OptIn(FlowPreview::class)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    private suspend fun getAddress(id: UUID): String {

        // 1. Check if we have a saved address for this device from a previous connection.
        val storedAddress = dataStore.data.firstOrNull()?.get(stringPreferencesKey(id.toString()))
        if (storedAddress != null) {
            return storedAddress
        }

        Log.d(TAG, "Scanning for device: $id (instance: ${this.hashCode()})")

        // 2. Scan for the device by manufacturer data, timing out after 30 seconds if not found.
        val scanner = Scanner {
            filters {
                match {
                    manufacturerData = listOf(Filter.ManufacturerData(id = MANUFACTURER_ID, data = id. toByteArray()))
                }
            }
        }

        // 3. Wait for the first advertisement that matches the filter, or throw a NotFoundException if none is found within the timeout.
        val advertisement = scanner.advertisements
            .timeout(30.seconds)
            .catch { e ->

                if (e is TimeoutCancellationException) {
                    Log.w(TAG, "Device not found within timeout: $id")
                    throw NotFoundException(id)
                }

                throw e
            }
            .firstOrNull() ?: run {
            Log.w(TAG, "Device not found: $id")
            throw NotFoundException(id)
        }

        Log.d(TAG, "Device found: ${advertisement.address}")

        // 4. Return the address of the found device.
        return advertisement.address
    }

    /**
     * Bonds with [device] if it isn't already bonded, suspending until bonding completes.
     *
     * @throws InternalErrorException if bonding fails to start or complete.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun bond(device: BluetoothDevice) {

        // 1. If the device is already bonded, return immediately.
        if (device.bondState != BluetoothDevice.BOND_NONE) {
            return
        }

        Log.d(TAG, "Device at address: ${device.address} is not bonded, initiating bonding process")

        // 2. Initiate bonding with the device.
        val bonding = device.createBond()

        if (!bonding) {
            Log.w(TAG, "createBond() returned false for address: ${device.address}")
            throw InternalErrorException("Failed to create bond with device at address: ${device.address}")
        }

        Log.d(TAG, "Bonding initiated for address: ${device.address}, waiting for bond state change")

        // 3. Wait for the bond state to change to either BONDED or BOND_NONE.
        val bondState = withTimeoutOrNull(30.seconds) { awaitBondState(device.address) }

        if (bondState != BluetoothDevice.BOND_BONDED) {
            Log.w(TAG, "Bonding failed for address: ${device.address}, final state: $bondState")
            throw InternalErrorException("Failed to bond with device at address: ${device.address}")
        }

        Log.d(TAG, "Bonded successfully with address: ${device.address}")
    }

    /**
     * Resolves the device advertising [id] as manufacturer data and connects to it once found.
     *
     * @throws NotFoundException if the device is not found within the scan timeout.
     * @throws InternalErrorException if bonding or GATT connection fails.
     */
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override suspend fun connect(id: UUID) {

        // 1. Get the device's address, either from a previous connection or by scanning for it.
        val address = getAddress(id)

        Log.d(TAG, "Attempting to connect to: $address")

        // 2. Get the BluetoothDevice object for the address.
        val device = adapter.getRemoteDevice(address)

        bond(device)

        Log.d(TAG, "Device at address: $address is bonded, proceeding to connect to GATT server")

        // 3. Connect to the GATT server; Kable discovers services automatically as part of connect().
        val peripheral = Peripheral(address)
        peripheral.connect()
        this.peripheral = peripheral

        // 4. Remember this device's address so future connects can skip scanning for it.
        dataStore.edit { preferences -> preferences[stringPreferencesKey(id.toString())] = address }
    }

    override suspend fun read(serviceId: Uuid, characteristicId: Uuid): ByteArray {

        Log.d(TAG, "Reading characteristic: $characteristicId (service: $serviceId)")

        val peripheral = peripheral ?: run {
            Log.w(TAG, "Cannot read $characteristicId: not connected to a GATT server")
            throw InternalErrorException("Not connected to a GATT server")
        }

        return peripheral.read(characteristicOf(serviceId, characteristicId))
    }

    override suspend fun write(serviceId: Uuid, characteristicId: Uuid, value: ByteArray) {

        Log.d(TAG, "Writing characteristic: $characteristicId (service: $serviceId), ${value.size} bytes")

        val peripheral = peripheral ?: run {
            Log.w(TAG, "Cannot write $characteristicId: not connected to a GATT server")
            throw InternalErrorException("Not connected to a GATT server")
        }

        peripheral.write(
            characteristicOf(serviceId, characteristicId),
            value,
            WriteType.WithResponse
        )
    }

    override fun close() {

        Log.d(TAG, "Closing device connection")

        peripheral?.close()
        peripheral = null

    }

    private fun UUID.toByteArray(): ByteArray {

        val buffer = ByteBuffer.allocate(16)

        buffer.putLong(this.mostSignificantBits)
        buffer.putLong(this.leastSignificantBits)

        return buffer.array()
    }

}
