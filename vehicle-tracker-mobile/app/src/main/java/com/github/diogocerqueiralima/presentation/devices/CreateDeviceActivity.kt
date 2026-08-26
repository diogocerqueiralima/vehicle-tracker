package com.github.diogocerqueiralima.presentation.devices

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
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

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onCameraPermissionResult(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.onCameraPermissionResult(
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )

        setContent {
            CreateDeviceScreen(
                viewModel = viewModel,
                onRequestCameraPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                onDeviceCreated = { finish() },
                onBack = { finish() }
            )
        }

    }

}
