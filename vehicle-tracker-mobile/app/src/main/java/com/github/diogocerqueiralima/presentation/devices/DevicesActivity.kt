package com.github.diogocerqueiralima.presentation.devices

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.github.diogocerqueiralima.DependenciesContainer
import com.github.diogocerqueiralima.domain.services.DeviceService
import com.github.diogocerqueiralima.infrastructure.repositories.DeviceRepositoryImpl
import com.github.diogocerqueiralima.presentation.ui.activities.BottomNavigationActivity
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationDestination
import com.github.diogocerqueiralima.presentation.devices.screens.DevicesScreen
import com.github.diogocerqueiralima.presentation.devices.viewmodel.DevicesViewModel
import com.github.diogocerqueiralima.presentation.devices.viewmodel.DevicesViewModelFactory

/**
 * Activity that displays the devices screen and handles navigation to other destinations.
 */
class DevicesActivity : BottomNavigationActivity(BottomNavigationDestination.Devices) {

    private val viewModel by viewModels<DevicesViewModel>(
        factoryProducer = {

            val dependenciesContainer = application as DependenciesContainer
            val deviceRepository = DeviceRepositoryImpl(dependenciesContainer.httpClient)
            val deviceService = DeviceService(deviceRepository)

            DevicesViewModelFactory(deviceService)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DevicesScreen(
                viewModel = viewModel,
                onNavigate = ::navigateTo,
                onAddDevice = { startActivity(Intent(this, CreateDeviceActivity::class.java)) }
            )
        }

        viewModel.loadDevices()
    }

}
