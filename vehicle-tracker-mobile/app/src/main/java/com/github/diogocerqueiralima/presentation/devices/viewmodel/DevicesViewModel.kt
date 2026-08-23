package com.github.diogocerqueiralima.presentation.devices.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.diogocerqueiralima.domain.model.Device
import com.github.diogocerqueiralima.domain.services.DeviceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the different states of the devices list retrieval process.
 */
sealed interface DevicesState {
    data object Loading : DevicesState
    data class Loaded(val devices: List<Device>) : DevicesState
    data class Error(val message: String) : DevicesState
}

class DevicesViewModel(
    initialState: DevicesState,
    val deviceService: DeviceService
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<DevicesState> = _state.asStateFlow()

    /**
     * Loads the list of devices.
     */
    fun loadDevices() {

        _state.value = DevicesState.Loading

        viewModelScope.launch {
            try {
                val devices = deviceService.findAll()
                _state.value = DevicesState.Loaded(devices)
            } catch (exception: Exception) {
                _state.value = DevicesState.Error(
                    message = exception.message ?: "An unexpected error occurred while loading devices."
                )
            }
        }

    }

}

/**
 * Factory class for creating instances of DevicesViewModel with the required dependencies.
 */
@Suppress("UNCHECKED_CAST")
class DevicesViewModelFactory(
    val deviceService: DeviceService,
    val initialState: DevicesState = DevicesState.Loading
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DevicesViewModel(
            deviceService = deviceService,
            initialState = initialState
        ) as T
    }

}
