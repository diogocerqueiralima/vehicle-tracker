package com.github.diogocerqueiralima.presentation.devices.viewmodel

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.domain.devices.services.DeviceService
import com.github.diogocerqueiralima.domain.authentication.services.UserSessionService
import com.github.diogocerqueiralima.presentation.errors.CommonReason
import com.github.diogocerqueiralima.presentation.errors.Reason
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

const val CREATE_DEVICE_VIEW_MODEL_TAG = "CREATE_DEVICE_VIEW_MODEL"

/**
 * Represents the current values of the device creation form.
 */
data class CreateDeviceFormState(
    val id: UUID? = null,
    val serialNumber: String = "",
    val model: String = "",
    val manufacturer: String = "",
    val imei: String = ""
) {

    val isValid: Boolean
        get() = id != null && serialNumber.isNotBlank() && model.isNotBlank() && manufacturer.isNotBlank() && imei.isNotBlank()

}

/**
 * Errors specific to the device creation flow.
 */
enum class CreateDeviceReason : Reason {
    CAMERA_PERMISSION_DENIED,
    CAMERA_UNAVAILABLE,
    INVALID_QR_CODE,
    QR_PROCESSING_FAILED
}

/**
 * Represents the different states of the device creation process.
 */
sealed interface CreateDeviceState {
    object Idle : CreateDeviceState
    data class Scanning(val cameraProvider: ListenableFuture<ProcessCameraProvider>): CreateDeviceState
    data class Filling(val form: CreateDeviceFormState) : CreateDeviceState
    data class Submitting(val form: CreateDeviceFormState) : CreateDeviceState
    data class Success(val device: Device) : CreateDeviceState
    data class Error(val form: CreateDeviceFormState = CreateDeviceFormState(), val reason: Reason) : CreateDeviceState
}

class CreateDeviceViewModel(
    private val deviceService: DeviceService,
    private val userSessionService: UserSessionService
) : ViewModel() {

    private val _state = MutableStateFlow<CreateDeviceState>(CreateDeviceState.Idle)
    val state: StateFlow<CreateDeviceState> = _state.asStateFlow()

    /**
     * Called when the camera permission required to scan the device QR code is granted or denied.
     * From [CreateDeviceState.Idle], this also moves the flow into [CreateDeviceState.Scanning].
     */
    fun onCameraPermissionResult(granted: Boolean, cameraProvider: ListenableFuture<ProcessCameraProvider>) {

        val state = _state.value
        if (state !is CreateDeviceState.Idle) {
            return
        }

        if (granted) {
            _state.value = CreateDeviceState.Scanning(cameraProvider)
        } else {
            _state.value = CreateDeviceState.Error(form = CreateDeviceFormState(), reason = CreateDeviceReason.CAMERA_PERMISSION_DENIED)
        }

    }

    /**
     * Called when the camera could not be bound for scanning, e.g. because the device has none.
     */
    fun onCameraUnavailable() {

        val state = _state.value
        if (state !is CreateDeviceState.Scanning) {
            return
        }

        _state.value = CreateDeviceState.Error(form = CreateDeviceFormState(), reason = CreateDeviceReason.CAMERA_UNAVAILABLE)
    }

    fun onSerialNumberChange(serialNumber: String) = updateForm { it.copy(serialNumber = serialNumber) }

    fun onModelChange(model: String) = updateForm { it.copy(model = model) }

    fun onManufacturerChange(manufacturer: String) = updateForm { it.copy(manufacturer = manufacturer) }

    fun onImeiChange(imei: String) = updateForm { it.copy(imei = imei) }

    private fun updateForm(transform: (CreateDeviceFormState) -> CreateDeviceFormState) {

        val form = when (val state = _state.value) {
            is CreateDeviceState.Filling -> state.form
            is CreateDeviceState.Error -> state.form
            is CreateDeviceState.Idle, is CreateDeviceState.Scanning, is CreateDeviceState.Submitting, is CreateDeviceState.Success -> return
        }

        _state.value = CreateDeviceState.Filling(transform(form))
    }

    /**
     * Creates a new device, from the current form values, owned by the currently authenticated user.
     *
     * @param onDeviceCreated Callback invoked with the created device once creation succeeds.
     */
    fun createDevice(onDeviceCreated: (Device) -> Unit) {

        val state = _state.value
        if (state !is CreateDeviceState.Filling) {
            return
        }

        val form = state.form
        if (!form.isValid) {
            return
        }

        _state.value = CreateDeviceState.Submitting(form)

        viewModelScope.launch {

            try {

                val session = userSessionService.get()
                if (session == null) {
                    _state.value = CreateDeviceState.Error(form = form, reason = CommonReason.NO_ACTIVE_SESSION)
                    return@launch
                }

                val ownerId = session.identity.id

                val device = deviceService.createOrUpdate(
                    id = form.id!!,
                    serialNumber = form.serialNumber,
                    model = form.model,
                    manufacturer = form.manufacturer,
                    imei = form.imei,
                    ownerId = ownerId
                )

                _state.value = CreateDeviceState.Success(device)
                onDeviceCreated(device)
            } catch (exception: Exception) {
                Log.e(CREATE_DEVICE_VIEW_MODEL_TAG, "Failed to create device", exception)
                _state.value = CreateDeviceState.Error(form = form, reason = CommonReason.UNEXPECTED_ERROR)
            }

        }

    }

    /**
     *
     * Processes an image from the camera, attempting to detect and decode a QR code representing a device identifier.
     *
     * @param proxy The [ImageProxy] containing the image data to be processed.
     */
    @OptIn(ExperimentalGetImage::class)
    fun processImage(proxy: ImageProxy, scanner: BarcodeScanner) {

        val state = _state.value
        if (state !is CreateDeviceState.Scanning) {
            proxy.close()
            return
        }

        val image = proxy.image
        if (image == null) {
            proxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(image, proxy.imageInfo.rotationDegrees)

        scanner.process(inputImage)
            .addOnSuccessListener { barCodes ->

                for (barcode in barCodes) {

                    val rawValue = barcode.rawValue

                    if (rawValue != null) {

                        try {
                            val id = UUID.fromString(rawValue)
                            _state.value = CreateDeviceState.Filling(CreateDeviceFormState(id = id))
                        } catch (e: IllegalArgumentException) {
                            Log.e(CREATE_DEVICE_VIEW_MODEL_TAG, "Scanned QR code is not a valid device identifier: $rawValue", e)
                            _state.value = CreateDeviceState.Error(form = CreateDeviceFormState(), reason = CreateDeviceReason.INVALID_QR_CODE)
                        }

                        break
                    }
                }

            }
            .addOnFailureListener { exception ->
                Log.e(CREATE_DEVICE_VIEW_MODEL_TAG, "Failed to process the scanned QR code", exception)
                _state.value = CreateDeviceState.Error(form = CreateDeviceFormState(), reason = CreateDeviceReason.QR_PROCESSING_FAILED)
            }
            .addOnCompleteListener {
                proxy.close()
            }

    }

}

/**
 * Factory class for creating instances of CreateDeviceViewModel with the required dependencies.
 */
@Suppress("UNCHECKED_CAST")
class CreateDeviceViewModelFactory(
    private val deviceService: DeviceService,
    private val userSessionService: UserSessionService,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateDeviceViewModel(deviceService, userSessionService) as T
    }

}
