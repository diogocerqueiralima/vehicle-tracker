@file:OptIn(ExperimentalUuidApi::class)

package com.github.diogocerqueiralima.presentation.devices.viewmodel

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.diogocerqueiralima.domain.common.exceptions.InternalErrorException
import com.github.diogocerqueiralima.domain.common.exceptions.InvalidValueException
import com.github.diogocerqueiralima.domain.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.domain.devices.catalog.CharacteristicFormat
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

/** Which way a `FILE` characteristic's in-progress action is moving bytes. */
enum class FileDirection { DOWNLOAD, UPLOAD }

/**
 * Why a `FILE` characteristic's download or upload could not complete, carried by
 * [FileActionState.Failed].
 */
enum class FileActionFailureReason {

    /** Downloading a characteristic the device has no value configured for yet. */
    NOT_CONFIGURED,

    /** The device refused an uploaded value as invalid for the characteristic. */
    INVALID_VALUE,

    /** The transfer itself failed (connection, storage, or picked file access). */
    ACCESS_FAILED

}

/**
 * State of a single `FILE` characteristic's download/upload action. Keyed by
 * [CharacteristicSpec.key] in [DeviceConfigurationViewModel.fileActionStates]. Absent from the map
 * means no action has been run yet for that characteristic.
 */
sealed interface FileActionState {

    data class Running(val characteristic: CharacteristicSpec, val direction: FileDirection) : FileActionState
    data class Downloaded(val savedName: String) : FileActionState
    data object Uploaded : FileActionState
    data class Failed(val reason: FileActionFailureReason) : FileActionState

}

class DeviceConfigurationViewModel(
    private val deviceConfigurationService: DeviceConfigurationService,
    private val contentResolver: ContentResolver
) : ViewModel() {

    private val _state = MutableStateFlow<DeviceConfigurationState>(DeviceConfigurationState.Idle)
    val state: StateFlow<DeviceConfigurationState> = _state.asStateFlow()

    private val _characteristicValues = MutableStateFlow<Map<String, CharacteristicValueState>>(emptyMap())
    val characteristicValues: StateFlow<Map<String, CharacteristicValueState>> = _characteristicValues.asStateFlow()

    private val _fileActionStates = MutableStateFlow<Map<String, FileActionState>>(emptyMap())
    val fileActionStates: StateFlow<Map<String, FileActionState>> = _fileActionStates.asStateFlow()

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
            .filter { it.readable && it.format != CharacteristicFormat.FILE }
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

    /**
     * Downloads [characteristic]'s current value to a new entry in the user's Downloads
     * collection, named `<characteristic.name>.pem`, updating [fileActionStates] to reflect the
     * outcome. Does nothing if a download or upload for [characteristic] is already running.
     *
     * @param characteristic The `FILE` characteristic to download.
     */
    fun downloadFile(characteristic: CharacteristicSpec) {

        if (characteristic.format != CharacteristicFormat.FILE) {
            return
        }

        if (_fileActionStates.value[characteristic.key] is FileActionState.Running) {
            return
        }

        _fileActionStates.value += characteristic.key to FileActionState.Running(characteristic, FileDirection.DOWNLOAD)

        viewModelScope.launch {

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "${characteristic.name}.pem")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/x-pem-file")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val result = try {

                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw InternalErrorException("Could not create a Downloads entry")

                try {
                    val sink = contentResolver.openOutputStream(uri)
                        ?: throw InternalErrorException("Could not open the Downloads entry for writing")

                    sink.use { deviceConfigurationService.downloadFile(characteristic, it) }

                    FileActionState.Downloaded(savedName(uri) ?: "${characteristic.name}.pem")
                } catch (exception: Exception) {
                    contentResolver.delete(uri, null, null)
                    throw exception
                }

            } catch (exception: NotFoundException) {
                Log.d(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Characteristic is not configured yet: ${characteristic.key}", exception)
                FileActionState.Failed(FileActionFailureReason.NOT_CONFIGURED)
            } catch (exception: Exception) {
                Log.e(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Failed to download characteristic: ${characteristic.key}", exception)
                FileActionState.Failed(FileActionFailureReason.ACCESS_FAILED)
            }

            _fileActionStates.value += characteristic.key to result
        }

    }

    /**
     * Marks [characteristic] as awaiting a picked file, so [onFilePicked] knows which
     * characteristic to upload the result to once the file picker returns. The caller is expected
     * to launch the picker right after calling this. Does nothing if a download or upload for
     * [characteristic] is already running.
     *
     * @param characteristic The `FILE` characteristic to write the picked file to.
     */
    fun requestUpload(characteristic: CharacteristicSpec) {

        if (characteristic.format != CharacteristicFormat.FILE || !characteristic.writable) {
            return
        }

        if (_fileActionStates.value[characteristic.key] is FileActionState.Running) {
            return
        }

        _fileActionStates.value += characteristic.key to FileActionState.Running(characteristic, FileDirection.UPLOAD)
    }

    /**
     * Called with the file picker's result, uploading it to whichever characteristic
     * [requestUpload] last marked as awaiting one, updating [fileActionStates] to reflect the
     * outcome. A `null` [uri] (the picker was dismissed without a selection) clears that marker
     * instead of uploading anything. Does nothing if no characteristic is currently awaiting one.
     *
     * @param uri The picked file, as returned by the Storage Access Framework, or `null` if none was picked.
     */
    fun onFilePicked(uri: Uri?) {

        val characteristic = _fileActionStates.value.values
            .filterIsInstance<FileActionState.Running>()
            .firstOrNull { it.direction == FileDirection.UPLOAD }
            ?.characteristic
            ?: return

        if (uri == null) {
            _fileActionStates.value -= characteristic.key
            return
        }

        viewModelScope.launch {

            val result = try {

                val length = fileSize(uri) ?: throw InternalErrorException("Could not determine the file's size")
                val source = contentResolver.openInputStream(uri)
                    ?: throw InternalErrorException("Could not open the picked file for reading")

                source.use { deviceConfigurationService.uploadFile(characteristic, it, length) }

                FileActionState.Uploaded
            } catch (exception: InvalidValueException) {
                Log.w(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Device rejected uploaded value for: ${characteristic.key}", exception)
                FileActionState.Failed(FileActionFailureReason.INVALID_VALUE)
            } catch (exception: Exception) {
                Log.e(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Failed to upload characteristic: ${characteristic.key}", exception)
                FileActionState.Failed(FileActionFailureReason.ACCESS_FAILED)
            }

            _fileActionStates.value += characteristic.key to result
        }

    }

    /**
     * The display name [uri] was actually saved under, which MediaStore may have changed from the
     * one requested on a name collision, or `null` if it can't be determined.
     */
    private fun savedName(uri: Uri): String? =
        contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

    /**
     * The size, in bytes, of the file at [uri], or `null` if it can't be determined.
     */
    private fun fileSize(uri: Uri): Long? =
        contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else null
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
    private val contentResolver: ContentResolver
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DeviceConfigurationViewModel(deviceConfigurationService, contentResolver) as T
    }

}