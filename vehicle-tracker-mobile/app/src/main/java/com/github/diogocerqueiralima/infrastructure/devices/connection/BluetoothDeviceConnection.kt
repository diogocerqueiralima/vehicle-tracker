package com.github.diogocerqueiralima.infrastructure.devices.connection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.github.diogocerqueiralima.domain.common.exceptions.InternalErrorException
import com.github.diogocerqueiralima.domain.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.domain.devices.connection.DeviceConnection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

private const val TAG = "BLUETOOTH_DEVICE_CONNECTION"

/**
 * Implementation of [DeviceConnection] for Bluetooth devices.
 *
 * Registers a single [BroadcastReceiver] for bond state changes on init.
 * GATT callbacks (service discovery, characteristic reads/writes) follow the same
 * shared-flow bridging as bond state: the callback emits, and each operation awaits the
 * matching event via `filter`/`first`.
 */
class BluetoothDeviceConnection(
    private val context: Context,
    private val adapter: BluetoothAdapter
) : DeviceConnection {

    constructor(context: Context, bluetoothManager: BluetoothManager) : this(context, bluetoothManager.adapter)

    private var gatt: BluetoothGatt? = null

    private val bondStateChanges = MutableSharedFlow<BondStateChange>(extraBufferCapacity = 1)

    private val servicesDiscovered = MutableSharedFlow<Boolean>(replay = 1)

    private val characteristicReads = MutableSharedFlow<CharacteristicRead>(extraBufferCapacity = 1)
    private val characteristicWrites = MutableSharedFlow<CharacteristicWrite>(extraBufferCapacity = 1)

    private val bondStateReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            Log.d(TAG, "onReceive: action=${intent.action} (instance: ${this@BluetoothDeviceConnection.hashCode()})")

            val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)

            if (device == null) {
                Log.w(TAG, "onReceive: EXTRA_DEVICE was null, dropping broadcast")
                return
            }

            val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)

            Log.d(TAG, "Bond state changed for ${device.address}: $state")

            bondStateChanges.tryEmit(BondStateChange(device.address, state))
        }

    }

    private val gattCallback = object : BluetoothGattCallback() {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {

            Log.d(TAG, "GATT connection state changed: status=$status, newState=$newState")

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                g.discoverServices()
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered successfully")
            } else {
                Log.w(TAG, "Failed to discover services, status: $status")
            }

            servicesDiscovered.tryEmit(status == BluetoothGatt.GATT_SUCCESS)
        }

        override fun onCharacteristicRead(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
            Log.d(TAG, "Characteristic read: ${characteristic.uuid}, status: $status, ${value.size} bytes")
            characteristicReads.tryEmit(CharacteristicRead(characteristic.uuid, value, status))
        }

        override fun onCharacteristicWrite(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            Log.d(TAG, "Characteristic write: ${characteristic.uuid}, status: $status")
            characteristicWrites.tryEmit(CharacteristicWrite(characteristic.uuid, status))
        }

    }

    init {

        Log.d(TAG, "Registering bond state receiver on context: $context (instance: ${this.hashCode()})")

        ContextCompat.registerReceiver(
            context,
            bondStateReceiver,
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )

        Log.d(TAG, "Bond state receiver registered")
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun connect(address: String) {

        // 1. Get the remote Bluetooth device using the provided address.
        val device = adapter.getRemoteDevice(address)

        Log.d(TAG, "Connecting to device at address: $address (instance: ${this.hashCode()})")

        if (device.bondState == BluetoothDevice.BOND_NONE) {

            Log.d(TAG, "Device at address: $address is not bonded, initiating bonding process")

            // 2. Initiate bonding with the device.
            val bonding = device.createBond()

            if (!bonding) {
                Log.w(TAG, "createBond() returned false for address: $address")
                throw InternalErrorException("Failed to create bond with device at address: $address")
            }

            Log.d(TAG, "Bonding initiated for address: $address, waiting for bond state change")

            // 3. Wait for the bond state to change to either BONDED or BOND_NONE.
            val bondState = bondStateChanges
                .filter { it.address == address }
                .map { it.state }
                .firstOrNull { it != BluetoothDevice.BOND_BONDING }

            if (bondState != BluetoothDevice.BOND_BONDED) {
                Log.w(TAG, "Bonding failed for address: $address, final state: $bondState")
                throw InternalErrorException("Failed to bond with device at address: $address")
            }

            Log.d(TAG, "Bonded successfully with address: $address, connecting to GATT server")
        }

        Log.d(TAG, "Device at address: $address is bonded, proceeding to connect to GATT server")

        // 4. Connect to the GATT server on the device, keeping the handle for later operations.
        gatt = device.connectGatt(context, false, gattCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun read(serviceId: UUID, characteristicId: UUID): ByteArray {

        Log.d(TAG, "Reading characteristic: $characteristicId (service: $serviceId)")

        val gatt = gatt ?: run {
            Log.w(TAG, "Cannot read $characteristicId: not connected to a GATT server")
            throw InternalErrorException("Not connected to a GATT server")
        }

        // 1. Wait until services have been discovered before looking anything up.
        if (servicesDiscovered.first().not()) {
            throw InternalErrorException("Failed to discover services")
        }

        Log.d(TAG, gatt.services.joinToString(", ") { it.uuid.toString() })
        Log.d(TAG, gatt.services.flatMap { it.characteristics }.joinToString(", ") { it.uuid.toString() })

        // 2. Find the target characteristic.
        val characteristic = gatt.getService(serviceId)?.getCharacteristic(characteristicId)
            ?: throw NotFoundException(characteristicId)

        // 3. Start the read; the actual value arrives later via the callback.
        if (!gatt.readCharacteristic(characteristic)) {
            Log.w(TAG, "gatt.readCharacteristic() returned false for: $characteristicId")
            throw InternalErrorException("Failed to start read for characteristic: $characteristicId")
        }

        val read = characteristicReads.first { it.characteristicId == characteristicId }

        if (read.status != BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, "Read failed for characteristic: $characteristicId, status: ${read.status}")
            throw InternalErrorException("Failed to read characteristic: $characteristicId, status: ${read.status}")
        }

        return read.value

    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun write(serviceId: UUID, characteristicId: UUID, value: ByteArray) {

        Log.d(TAG, "Writing characteristic: $characteristicId (service: $serviceId), ${value.size} bytes")

        val gatt = gatt ?: run {
            Log.w(TAG, "Cannot write $characteristicId: not connected to a GATT server")
            throw InternalErrorException("Not connected to a GATT server")
        }

        // 1. Wait until services have been discovered before looking anything up.
        if (servicesDiscovered.first().not()) {
            throw InternalErrorException("Failed to discover services")
        }

        // 2. Find the target characteristic.
        val characteristic = gatt.getService(serviceId)?.getCharacteristic(characteristicId)
            ?: throw NotFoundException(characteristicId)

            // 3. Start the write; the framework confirms/rejects it later via the callback.
        val started = gatt.writeCharacteristic(
            characteristic,
            value,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ) == BluetoothStatusCodes.SUCCESS

        if (!started) {
            Log.w(TAG, "gatt.writeCharacteristic() failed to start for: $characteristicId")
            throw InternalErrorException("Failed to start write for characteristic: $characteristicId")
        }

        val write = characteristicWrites.first { it.characteristicId == characteristicId }

        if (write.status != BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, "Write failed for characteristic: $characteristicId, status: ${write.status}")
            throw InternalErrorException("Failed to write characteristic: $characteristicId, status: ${write.status}")
        }

    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun close() {

        Log.d(TAG, "Closing device connection")

        context.unregisterReceiver(bondStateReceiver)

        gatt?.disconnect()
        gatt?.close()
        gatt = null

    }

    private data class BondStateChange(val address: String, val state: Int)

    private data class CharacteristicWrite(val characteristicId: UUID, val status: Int)

    private data class CharacteristicRead(val characteristicId: UUID, val value: ByteArray, val status: Int) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CharacteristicRead

            if (status != other.status) return false
            if (characteristicId != other.characteristicId) return false
            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = status
            result = 31 * result + characteristicId.hashCode()
            result = 31 * result + value.contentHashCode()
            return result
        }

    }

}
