@file:OptIn(ExperimentalUuidApi::class)

package com.github.diogocerqueiralima.presentation.devices.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.diogocerqueiralima.domain.common.exceptions.BadRequestException
import com.github.diogocerqueiralima.domain.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.domain.devices.catalog.CharacteristicFormat
import com.github.diogocerqueiralima.domain.devices.catalog.CharacteristicSpec
import com.github.diogocerqueiralima.domain.devices.catalog.ServiceSpec
import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.domain.devices.services.DeviceConfigurationService
import com.github.diogocerqueiralima.domain.devices.services.DeviceEnrollmentService
import com.github.diogocerqueiralima.domain.devices.services.EnrollmentStep
import com.github.diogocerqueiralima.domain.devices.catalog.CharacteristicCodec
import com.github.diogocerqueiralima.presentation.errors.Reason
import kotlinx.coroutines.CancellationException
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
 * Why a characteristic's value could not be shown, carried by [CharacteristicValueState.Failed].
 */
enum class CharacteristicFailureReason {

    /** The device has no value configured for the characteristic yet. */
    NOT_CONFIGURED,

    /** The read or write itself failed. */
    ACCESS_FAILED

}

/**
 * State of a single characteristic's value, as read from the device. Keyed by
 * [CharacteristicSpec.key] in [DeviceConfigurationViewModel.characteristicValues].
 */
sealed interface CharacteristicValueState {

    data object Loading : CharacteristicValueState
    data class Loaded(val value: String) : CharacteristicValueState
    data class Failed(val reason: CharacteristicFailureReason) : CharacteristicValueState

}

/**
 * Why enrollment stopped, carried by [EnrollmentState.Failed] alongside the step it failed on.
 */
enum class EnrollmentFailureReason {

    /** The device already holds a certificate, and refused to hand out a request for a new one. */
    ALREADY_ENROLLED,

    /** The step itself failed. */
    STEP_FAILED

}

/**
 * State of the enrollment flow, which the user starts by tapping the request characteristic and
 * which runs the read, the signing and the install as one action. See [DeviceEnrollmentService].
 */
sealed interface EnrollmentState {

    /** Not started, or started and since reset by a new attempt. */
    data object Idle : EnrollmentState

    /** [step] is currently running. */
    data class Running(val step: EnrollmentStep) : EnrollmentState

    /** Every step succeeded and the certificate is installed on the device. */
    data object Enrolled : EnrollmentState

    /** [step] failed for [reason]; the device is as it was before the attempt. */
    data class Failed(val step: EnrollmentStep, val reason: EnrollmentFailureReason) : EnrollmentState

}

class DeviceConfigurationViewModel(
    private val deviceConfigurationService: DeviceConfigurationService,
    private val deviceEnrollmentService: DeviceEnrollmentService
) : ViewModel() {

    private val _state = MutableStateFlow<DeviceConfigurationState>(DeviceConfigurationState.Idle)
    val state: StateFlow<DeviceConfigurationState> = _state.asStateFlow()

    private val _characteristicValues = MutableStateFlow<Map<String, CharacteristicValueState>>(emptyMap())
    val characteristicValues: StateFlow<Map<String, CharacteristicValueState>> = _characteristicValues.asStateFlow()

    private val _enrollment = MutableStateFlow<EnrollmentState>(EnrollmentState.Idle)
    val enrollment: StateFlow<EnrollmentState> = _enrollment.asStateFlow()

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
     * Clickable characteristics are left out: the user acts on them rather than reading them, and
     * whether their value is fetched at all is up to what tapping them starts.
     *
     * @param service The service whose characteristics should be read.
     */
    fun readService(service: ServiceSpec) {
        service.characteristics
            .filter { it.readable && !it.clickable }
            .forEach { readCharacteristic(it) }
    }

    /**
     * Reads [characteristic] from the connected device and saves it to the user's downloads,
     * updating [characteristicValues] to reflect the outcome: [CharacteristicValueState.Loaded]
     * carries the name the file was saved under, not the value itself, which is never displayed.
     *
     * @param characteristic The characteristic to download. Must be a [CharacteristicFormat.FILE] one.
     */
    fun downloadCharacteristic(characteristic: CharacteristicSpec) {

        if (_characteristicValues.value[characteristic.key] is CharacteristicValueState.Loading) {
            return
        }

        _characteristicValues.value += characteristic.key to CharacteristicValueState.Loading

        viewModelScope.launch {

            val result = try {
                CharacteristicValueState.Loaded(deviceConfigurationService.download(characteristic))
            } catch (exception: NotFoundException) {
                Log.d(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Characteristic is not configured yet: ${characteristic.key}", exception)
                CharacteristicValueState.Failed(CharacteristicFailureReason.NOT_CONFIGURED)
            } catch (exception: CancellationException) {
                // The screen is going away, so there is no outcome to report.
                throw exception
            } catch (exception: Exception) {
                Log.e(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Failed to download characteristic: ${characteristic.key}", exception)
                CharacteristicValueState.Failed(CharacteristicFailureReason.ACCESS_FAILED)
            }

            _characteristicValues.value += characteristic.key to result
        }

    }

    /**
     * Runs the enrollment flow end to end, moving [enrollment] through each
     * [EnrollmentStep] as it runs and into [EnrollmentState.Enrolled] or
     * [EnrollmentState.Failed] once it settles. Failures carry the step they happened on, and
     * leave the device as it was, so calling this again retries the whole flow.
     *
     * This method is a no-op while a previous attempt is still running.
     */
    fun enroll() {

        if (_enrollment.value is EnrollmentState.Running) {
            return
        }

        // Claim the flow before suspending, so a second tap can't start a parallel attempt.
        var step = EnrollmentStep.READING_REQUEST
        _enrollment.value = EnrollmentState.Running(step)

        viewModelScope.launch {

            try {

                // Each emission is the step that just started, so the last one seen is the one a failure came from.
                deviceEnrollmentService.enroll().collect { current ->
                    step = current
                    _enrollment.value = EnrollmentState.Running(current)
                }

                Log.d(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Device enrolled")
                _enrollment.value = EnrollmentState.Enrolled

            } catch (exception: BadRequestException) {
                Log.d(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Device refused enrollment at step: $step", exception)
                _enrollment.value = EnrollmentState.Failed(step, EnrollmentFailureReason.ALREADY_ENROLLED)
            } catch (exception: CancellationException) {
                // The screen is going away, so there is no outcome to report.
                throw exception
            } catch (exception: Exception) {
                Log.e(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Failed to enroll the device at step: $step", exception)
                _enrollment.value = EnrollmentState.Failed(step, EnrollmentFailureReason.STEP_FAILED)
            }

        }

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
            } catch (exception: NotFoundException) {
                Log.d(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Characteristic is not configured yet: ${characteristic.key}", exception)
                CharacteristicValueState.Failed(CharacteristicFailureReason.NOT_CONFIGURED)
            } catch (exception: Exception) {
                Log.e(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Failed to read characteristic: ${characteristic.key}", exception)
                CharacteristicValueState.Failed(CharacteristicFailureReason.ACCESS_FAILED)
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
                CharacteristicValueState.Failed(CharacteristicFailureReason.ACCESS_FAILED)
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
    private val deviceConfigurationService: DeviceConfigurationService,
    private val deviceEnrollmentService: DeviceEnrollmentService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DeviceConfigurationViewModel(deviceConfigurationService, deviceEnrollmentService) as T
    }

}