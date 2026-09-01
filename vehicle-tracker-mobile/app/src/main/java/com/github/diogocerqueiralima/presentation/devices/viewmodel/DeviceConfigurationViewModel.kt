@file:OptIn(ExperimentalUuidApi::class)

package com.github.diogocerqueiralima.presentation.devices.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.diogocerqueiralima.domain.devices.catalog.CharacteristicSpec
import com.github.diogocerqueiralima.domain.devices.catalog.ServiceSpec
import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.domain.devices.services.DeviceConfigurationService
import com.github.diogocerqueiralima.domain.devices.catalog.CharacteristicCodec
import com.github.diogocerqueiralima.presentation.errors.Reason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

const val DEVICE_CONFIGURATION_VIEW_MODEL_TAG = "DEVICE_CONFIGURATION_VIEW_MODEL"

enum class DeviceConfigurationReason : Reason {

    CONNECTION_FAILED,
    BLUETOOTH_PERMISSION_DENIED

}

sealed interface DeviceConfigurationState {

    data object Idle : DeviceConfigurationState
    data class Connecting(val device: Device) : DeviceConfigurationState
    data class Connected(val device: Device) : DeviceConfigurationState
    data class Error(val reason: Reason) : DeviceConfigurationState

}

/**
 * State of a single characteristic's value, as read from the device. Keyed by
 * [CharacteristicSpec.key] in [DeviceConfigurationViewModel.characteristicValues].
 */
sealed interface CharacteristicValueState {

    data object Loading : CharacteristicValueState
    data class Loaded(val value: String) : CharacteristicValueState
    data object Failed : CharacteristicValueState

}

class DeviceConfigurationViewModel(
    private val deviceConfigurationService: DeviceConfigurationService
) : ViewModel() {

    private val _state = MutableStateFlow<DeviceConfigurationState>(DeviceConfigurationState.Idle)
    val state: StateFlow<DeviceConfigurationState> = _state.asStateFlow()

    private val _characteristicValues = MutableStateFlow<Map<String, CharacteristicValueState>>(emptyMap())
    val characteristicValues: StateFlow<Map<String, CharacteristicValueState>> = _characteristicValues.asStateFlow()

    /**
     * Called when the Bluetooth permissions required to connect to the device are granted or denied.
     * From [DeviceConfigurationState.Idle], this either starts the connection or moves the flow
     * into [DeviceConfigurationState.Error].
     *
     * @param granted Whether the Bluetooth permissions were granted.
     * @param device The device to connect to if permissions are granted.
     */
    fun onBluetoothPermissionResult(granted: Boolean, device: Device) {

        if (granted) {
            connect(device)
            return
        }

        if (_state.value !is DeviceConfigurationState.Idle) {
            return
        }

        _state.value = DeviceConfigurationState.Error(DeviceConfigurationReason.BLUETOOTH_PERMISSION_DENIED)
    }

    /**
     * Attempts to connect to the given [device]. If successful, moves the flow into [DeviceConfigurationState.Connected]. If unsuccessful, moves the flow into
     * [DeviceConfigurationState.Error] with [DeviceConfigurationReason.CONNECTION_FAILED].
     * This method is a no-op if the current state is not [DeviceConfigurationState.Idle].
     *
     * @param device The device to connect to.
     */
    fun connect(device: Device) {

        if (_state.value !is DeviceConfigurationState.Idle) {
            return
        }

        _state.value = DeviceConfigurationState.Connecting(device)

        viewModelScope.launch {

            try {
                Log.d(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Attempting to connect to device: ${device.id}")
                deviceConfigurationService.connect(device.id)
                _state.value = DeviceConfigurationState.Connected(device)
            } catch (exception: Exception) {
                Log.e(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Failed to connect to device", exception)
                _state.value = DeviceConfigurationState.Error(DeviceConfigurationReason.CONNECTION_FAILED)
            }

        }

    }

    /**
     * Reads every readable characteristic of [service], skipping ones already
     * loaded/loading. Meant to be called once, when a service's section is expanded.
     *
     * @param service The service whose characteristics should be read.
     */
    fun readService(service: ServiceSpec) {
        service.characteristics
            .filter { it.readable }
            .forEach { readCharacteristic(it) }
    }

    /**
     * Reads the value of [characteristic] from the connected device, updating
     * [characteristicValues] to reflect the outcome. If the characteristic is already loaded or loading, this method does nothing.
     *
     * @param characteristic The characteristic to read.
     */
    private fun readCharacteristic(characteristic: CharacteristicSpec) {

        if (_characteristicValues.value.containsKey(characteristic.key)) {
            return
        }

        _characteristicValues.value += characteristic.key to CharacteristicValueState.Loading

        viewModelScope.launch {

            val result = try {
                val bytes = deviceConfigurationService.read(characteristic.serviceId, characteristic.characteristicId)
                CharacteristicValueState.Loaded(CharacteristicCodec.decode(bytes, characteristic.format))
            } catch (exception: Exception) {
                Log.e(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Failed to read characteristic: ${characteristic.key}", exception)
                CharacteristicValueState.Failed
            }

            _characteristicValues.value += characteristic.key to result
        }

    }

    /**
     * Encodes [value] and writes it to [characteristic] on the connected device, updating
     * [characteristicValues] to reflect the outcome.
     *
     * @param characteristic The characteristic to write to.
     * @param value The value to write.
     */
    fun writeCharacteristic(characteristic: CharacteristicSpec, value: String) {

        _characteristicValues.value += characteristic.key to CharacteristicValueState.Loading

        viewModelScope.launch {

            val result = try {
                val bytes = CharacteristicCodec.encode(value, characteristic.format)
                deviceConfigurationService.write(characteristic.serviceId, characteristic.characteristicId, bytes)
                CharacteristicValueState.Loaded(value)
            } catch (exception: Exception) {
                Log.e(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Failed to write characteristic: ${characteristic.key}", exception)
                CharacteristicValueState.Failed
            }

            _characteristicValues.value += characteristic.key to result
        }

    }

    override fun onCleared() {
        super.onCleared()
        deviceConfigurationService.disconnect()
    }

}

/**
 * Factory class for creating instances of DeviceConfigurationViewModel with the required dependencies.
 */
@Suppress("UNCHECKED_CAST")
class DeviceConfigurationViewModelFactory(
    private val deviceConfigurationService: DeviceConfigurationService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DeviceConfigurationViewModel(deviceConfigurationService) as T
    }

}