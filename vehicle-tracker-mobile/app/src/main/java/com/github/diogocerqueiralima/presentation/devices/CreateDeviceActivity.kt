package com.github.diogocerqueiralima.presentation.devices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.github.diogocerqueiralima.DependenciesContainer
import com.github.diogocerqueiralima.domain.services.DeviceService
import com.github.diogocerqueiralima.infrastructure.repositories.DeviceRepositoryImpl
import com.github.diogocerqueiralima.presentation.devices.screens.CreateDeviceScreen
import com.github.diogocerqueiralima.presentation.devices.viewmodel.CreateDeviceViewModel
import com.github.diogocerqueiralima.presentation.devices.viewmodel.CreateDeviceViewModelFactory

/**
 * Activity that displays the create device screen.
 */
class CreateDeviceActivity : ComponentActivity() {

    private val viewModel by viewModels<CreateDeviceViewModel>(
        factoryProducer = {

            val dependenciesContainer = application as DependenciesContainer
            val deviceRepository = DeviceRepositoryImpl(dependenciesContainer.httpClient)
            val deviceService = DeviceService(deviceRepository)

            CreateDeviceViewModelFactory(deviceService, dependenciesContainer.userSessionService)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CreateDeviceScreen(
                viewModel = viewModel,
                onDeviceCreated = { finish() },
                onBack = { finish() }
            )
        }

    }

}
