package com.github.diogocerqueiralima.presentation.devices.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.domain.devices.services.DeviceConfigurationService
import com.github.diogocerqueiralima.presentation.errors.Reason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val DEVICE_CONFIGURATION_VIEW_MODEL_TAG = "DEVICE_CONFIGURATION_VIEW_MODEL"

enum class DeviceConfigurationReason : Reason {

    CONNECTION_FAILED

}

sealed interface DeviceConfigurationState {

    data object Idle : DeviceConfigurationState
    data class Connecting(val device: Device) : DeviceConfigurationState
    data class Connected(val device: Device) : DeviceConfigurationState
    data class Error(val reason: Reason) : DeviceConfigurationState

}

class DeviceConfigurationViewModel(
    private val deviceConfigurationService: DeviceConfigurationService
) : ViewModel() {

    private val _state = MutableStateFlow<DeviceConfigurationState>(DeviceConfigurationState.Idle)
    val state: StateFlow<DeviceConfigurationState> = _state.asStateFlow()

    fun connect(device: Device) {

        _state.value = DeviceConfigurationState.Connecting(device)

        viewModelScope.launch {
            try {
                deviceConfigurationService.connect(device.id)
                _state.value = DeviceConfigurationState.Connected(device)
            } catch (exception: Exception) {
                Log.e(DEVICE_CONFIGURATION_VIEW_MODEL_TAG, "Failed to connect to device", exception)
                _state.value = DeviceConfigurationState.Error(DeviceConfigurationReason.CONNECTION_FAILED)
            }
        }

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