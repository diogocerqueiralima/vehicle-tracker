package com.github.diogocerqueiralima.presentation.devices

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.github.diogocerqueiralima.DependenciesContainer
import com.github.diogocerqueiralima.domain.devices.services.DeviceService
import com.github.diogocerqueiralima.infrastructure.devices.repositories.DeviceRepositoryImpl
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

    private val cameraProvider by lazy {
        ProcessCameraProvider.getInstance(this)
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

        if (!granted && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // The system dialog won't be shown again for this permission, the user must grant it from Settings.
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            )
        }

        viewModel.onCameraPermissionResult(granted, cameraProvider)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            viewModel.onCameraPermissionResult(true, cameraProvider)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            CreateDeviceScreen(
                viewModel = viewModel,
                onDeviceCreated = { finish() },
                onBack = { finish() }
            )
        }

    }

}
