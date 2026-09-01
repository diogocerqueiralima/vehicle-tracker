package com.github.diogocerqueiralima.presentation.vehicles.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.diogocerqueiralima.domain.vehicles.model.Vehicle
import com.github.diogocerqueiralima.domain.vehicles.services.VehicleService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val VEHICLES_VIEW_MODEL_TAG = "VEHICLES_VIEW_MODEL"

/**
 * Represents the different states of the vehicles list retrieval process.
 */
sealed interface VehiclesState {
    data object Loading : VehiclesState
    data class Loaded(val vehicles: List<Vehicle>) : VehiclesState
    data class Error(val message: String) : VehiclesState
}

class VehiclesViewModel(
    initialState: VehiclesState,
    val vehicleService: VehicleService
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<VehiclesState> = _state.asStateFlow()

    /**
     * Loads the list of vehicles.
     */
    fun loadVehicles() {

        _state.value = VehiclesState.Loading

        viewModelScope.launch {
            try {
                val vehicles = vehicleService.findAll()
                _state.value = VehiclesState.Loaded(vehicles)
            } catch (exception: Exception) {
                Log.e(VEHICLES_VIEW_MODEL_TAG, "Failed to load vehicles", exception)
                _state.value = VehiclesState.Error(
                    message = exception.message ?: "An unexpected error occurred while loading vehicles."
                )
            }
        }

    }

}

/**
 * Factory class for creating instances of VehiclesViewModel with the required dependencies.
 */
@Suppress("UNCHECKED_CAST")
class VehiclesViewModelFactory(
    val vehicleService: VehicleService,
    val initialState: VehiclesState = VehiclesState.Loading
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VehiclesViewModel(
            vehicleService = vehicleService,
            initialState = initialState
        ) as T
    }

}
