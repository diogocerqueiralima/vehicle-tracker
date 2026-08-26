package com.github.diogocerqueiralima.presentation.devices.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.diogocerqueiralima.domain.model.Device
import com.github.diogocerqueiralima.domain.services.DeviceService
import com.github.diogocerqueiralima.domain.services.UserSessionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Represents the current values of the device creation form.
 */
data class CreateDeviceFormState(
    val id: UUID,
    val serialNumber: String = "",
    val model: String = "",
    val manufacturer: String = "",
    val imei: String = ""
) {

    val isValid: Boolean
        get() = serialNumber.isNotBlank() && model.isNotBlank() && manufacturer.isNotBlank() && imei.isNotBlank()

}

/**
 * Represents the different states of the device creation process.
 */
sealed interface CreateDeviceState {
    data class Scanning(val hasCameraPermission: Boolean = false, val invalidCodeScanned: Boolean = false) : CreateDeviceState
    data class Filling(val form: CreateDeviceFormState) : CreateDeviceState
    data class Submitting(val form: CreateDeviceFormState) : CreateDeviceState
    data class Success(val device: Device) : CreateDeviceState
    data class Error(val form: CreateDeviceFormState, val message: String) : CreateDeviceState
}

class CreateDeviceViewModel(
    private val deviceService: DeviceService,
    private val userSessionService: UserSessionService
) : ViewModel() {

    private val _state = MutableStateFlow<CreateDeviceState>(CreateDeviceState.Scanning())
    val state: StateFlow<CreateDeviceState> = _state.asStateFlow()

    /**
     * Called when the camera permission required to scan the device QR code is granted or denied.
     */
    fun onCameraPermissionResult(granted: Boolean) {

        val state = _state.value
        if (state !is CreateDeviceState.Scanning) return

        _state.value = state.copy(hasCameraPermission = granted)
    }

    /**
     * Called once a QR code has been decoded, moving the flow from scanning to filling the
     * remaining device details, or flagging the scanned code as invalid.
     *
     * @param id Device identifier decoded from the scanned QR code, or null if the code could not
     * be recognized as a device id.
     */
    fun onQrDecoded(id: UUID?) {

        val state = _state.value
        if (state !is CreateDeviceState.Scanning) return

        _state.value = if (id != null) {
            CreateDeviceState.Filling(CreateDeviceFormState(id = id))
        } else {
            state.copy(invalidCodeScanned = true)
        }
        
    }

    fun onSerialNumberChange(serialNumber: String) = updateForm { it.copy(serialNumber = serialNumber) }

    fun onModelChange(model: String) = updateForm { it.copy(model = model) }

    fun onManufacturerChange(manufacturer: String) = updateForm { it.copy(manufacturer = manufacturer) }

    fun onImeiChange(imei: String) = updateForm { it.copy(imei = imei) }

    private fun updateForm(transform: (CreateDeviceFormState) -> CreateDeviceFormState) {

        val form = when (val state = _state.value) {
            is CreateDeviceState.Filling -> state.form
            is CreateDeviceState.Error -> state.form
            is CreateDeviceState.Scanning, is CreateDeviceState.Submitting, is CreateDeviceState.Success -> return
        }

        _state.value = CreateDeviceState.Filling(transform(form))
    }

    /**
     * Creates a new device, from the current form values, owned by the currently authenticated user.
     *
     * @param onDeviceCreated Callback invoked with the created device once creation succeeds.
     */
    fun createDevice(onDeviceCreated: (Device) -> Unit) {

        val form = when (val state = _state.value) {
            is CreateDeviceState.Filling -> state.form
            is CreateDeviceState.Error -> state.form
            is CreateDeviceState.Scanning, is CreateDeviceState.Submitting, is CreateDeviceState.Success -> return
        }

        if (!form.isValid) return

        _state.value = CreateDeviceState.Submitting(form)

        viewModelScope.launch {

            try {

                val session = userSessionService.get()
                if (session == null) {
                    _state.value = CreateDeviceState.Error(
                        form = form,
                        message = "No active user session."
                    )
                    return@launch
                }

                val ownerId = session.identity.id

                val device = deviceService.createOrUpdate(
                    id = form.id,
                    serialNumber = form.serialNumber,
                    model = form.model,
                    manufacturer = form.manufacturer,
                    imei = form.imei,
                    ownerId = ownerId
                )

                _state.value = CreateDeviceState.Success(device)
                onDeviceCreated(device)
            } catch (exception: Exception) {
                _state.value = CreateDeviceState.Error(
                    form = form,
                    message = exception.message ?: "An unexpected error occurred while creating the device."
                )
            }

        }

    }

}

/**
 * Factory class for creating instances of CreateDeviceViewModel with the required dependencies.
 */
@Suppress("UNCHECKED_CAST")
class CreateDeviceViewModelFactory(
    private val deviceService: DeviceService,
    private val userSessionService: UserSessionService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateDeviceViewModel(deviceService, userSessionService) as T
    }

}
