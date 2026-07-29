package com.github.diogocerqueiralima.presentation.vehicles

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.github.diogocerqueiralima.DependenciesContainer
import com.github.diogocerqueiralima.domain.services.VehicleService
import com.github.diogocerqueiralima.infrastructure.repositories.VehicleRepositoryImpl
import com.github.diogocerqueiralima.presentation.ui.activities.BottomNavigationActivity
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationDestination
import com.github.diogocerqueiralima.presentation.vehicles.screens.VehiclesScreen
import com.github.diogocerqueiralima.presentation.vehicles.viewmodel.VehiclesViewModel
import com.github.diogocerqueiralima.presentation.vehicles.viewmodel.VehiclesViewModelFactory

/**
 * Activity that displays the vehicles screen and handles navigation to other destinations.
 */
class VehiclesActivity : BottomNavigationActivity(BottomNavigationDestination.Vehicles) {

    private val viewModel by viewModels<VehiclesViewModel>(
        factoryProducer = {

            val dependenciesContainer = application as DependenciesContainer
            val vehicleRepository = VehicleRepositoryImpl(dependenciesContainer.httpClient)
            val vehicleService = VehicleService(vehicleRepository)

            VehiclesViewModelFactory(vehicleService)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VehiclesScreen(viewModel = viewModel, onNavigate = ::navigateTo)
        }

        viewModel.loadVehicles()
    }

}
