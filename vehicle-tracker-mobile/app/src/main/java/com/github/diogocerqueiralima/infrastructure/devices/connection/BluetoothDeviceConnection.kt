@file:OptIn(ExperimentalUuidApi::class)

package com.github.diogocerqueiralima.infrastructure.devices.connection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
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
import kotlin.coroutines.resumeWithException
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
     * Registers a receiver for [BluetoothDevice.ACTION_BOND_STATE_CHANGED] and, once it's
     * listening, invokes [startBonding] to actually kick off bonding — so a fast bond-state
     * transition (e.g. a cached link key bonding near-instantly) can't be missed by starting
     * bonding before anything is listening for its result. Suspends until a non-BOND_BONDING
     * state for [address] is reported, returning that state.
     *
     * @throws Exception whatever [startBonding] throws, if it throws.
     */
    private suspend fun awaitBondState(address: String, startBonding: () -> Unit): Int = suspendCancellableCoroutine { continuation ->

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

        // The receiver is listening now, so it's safe to actually start bonding.
        try {
            startBonding()
        } catch (e: Exception) {
            context.unregisterReceiver(receiver)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Resolves the device advertising [id] as manufacturer data and connects to it once found.
     *
     * @throws NotFoundException if the device is not found within the scan timeout.
     * @throws InternalErrorException if bonding or GATT connection fails.
     */
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override suspend fun connect(id: UUID) {

        val storedAddress = getStoredAddress(id)

        // 1. If we have a stored address for this device, try to connect to it first.
        val address = if (storedAddress != null) {
            try {
                connectToAddress(storedAddress)
                storedAddress
            } catch (e: Exception) {
                // 2. If connecting to the stored address fails, clear it and scan for the device again.
                Log.w(TAG, "Failed to connect using stored address: $storedAddress, clearing it and re-scanning", e)
                dataStore.edit { preferences -> preferences.remove(addressKey(id)) }
                val scannedAddress = scanForAddress(id)
                connectToAddress(scannedAddress)
                scannedAddress
            }
        } else {
            // 3. If we don't have a stored address, scan for the device and connect to it.
            val scannedAddress = scanForAddress(id)
            connectToAddress(scannedAddress)
            scannedAddress
        }

        // 4. Remember this device's address so future connects can skip scanning for it.
        dataStore.edit { preferences -> preferences[addressKey(id)] = address }
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

    // Helper methods

    private fun addressKey(id: UUID) = stringPreferencesKey(id.toString())

    /**
     * Returns the MAC address saved for the device with the given [id] from a previous
     * connection, or `null` if none has been saved yet.
     */
    private suspend fun getStoredAddress(id: UUID): String? =
        dataStore.data.firstOrNull()?.get(addressKey(id))

    /**
     * Scans for the device advertising [id] as manufacturer data, timing out after 30 seconds if
     * not found.
     *
     * @throws NotFoundException if the device isn't found within the timeout.
     */
    @OptIn(FlowPreview::class)
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private suspend fun scanForAddress(id: UUID): String {

        Log.d(TAG, "Scanning for device: $id (instance: ${this.hashCode()})")

        val scanner = Scanner {
            filters {
                match {
                    manufacturerData = listOf(Filter.ManufacturerData(id = MANUFACTURER_ID, data = id.toByteArray()))
                }
            }
        }

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

        // 2 & 3. Start listening for bond state changes, then initiate bonding, then wait for the
        // bond state to change to either BONDED or BOND_NONE.
        val bondState = withTimeoutOrNull(30.seconds) {
            awaitBondState(device.address) {
                if (!device.createBond()) {
                    Log.w(TAG, "createBond() returned false for address: ${device.address}")
                    throw InternalErrorException("Failed to create bond with device at address: ${device.address}")
                }
                Log.d(TAG, "Bonding initiated for address: ${device.address}, waiting for bond state change")
            }
        }

        if (bondState != BluetoothDevice.BOND_BONDED) {
            Log.w(TAG, "Bonding failed for address: ${device.address}, final state: $bondState")
            throw InternalErrorException("Failed to bond with device at address: ${device.address}")
        }

        Log.d(TAG, "Bonded successfully with address: ${device.address}")
    }

    /**
     * Bonds with (if needed) and connects to the GATT server at [address]. On success, sets
     * [peripheral] to the connected [Peripheral].
     */
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT])
    private suspend fun connectToAddress(address: String) {

        Log.d(TAG, "Attempting to connect to: $address")

        val device = adapter.getRemoteDevice(address)

        bond(device)

        Log.d(TAG, "Device at address: $address is bonded, proceeding to connect to GATT server")

        // Kable discovers services automatically as part of connect().
        val peripheral = Peripheral(address)
        peripheral.connect()
        this.peripheral = peripheral
    }

    private fun UUID.toByteArray(): ByteArray {

        val buffer = ByteBuffer.allocate(16)

        buffer.putLong(this.mostSignificantBits)
        buffer.putLong(this.leastSignificantBits)

        return buffer.array()
    }

}
