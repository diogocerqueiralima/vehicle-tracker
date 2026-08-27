package com.github.diogocerqueiralima.presentation.devices.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.domain.model.Device
import com.github.diogocerqueiralima.presentation.devices.viewmodel.CreateDeviceError
import com.github.diogocerqueiralima.presentation.devices.viewmodel.CreateDeviceState
import com.github.diogocerqueiralima.presentation.devices.viewmodel.CreateDeviceViewModel
import com.github.diogocerqueiralima.presentation.devices.views.CreateDeviceErrorView
import com.github.diogocerqueiralima.presentation.devices.views.CreateDeviceSubmittingView
import com.github.diogocerqueiralima.presentation.devices.views.CreateDeviceSuccessView
import com.github.diogocerqueiralima.presentation.devices.views.CreateDeviceView
import com.github.diogocerqueiralima.presentation.devices.views.IdleView
import com.github.diogocerqueiralima.presentation.devices.views.ScanDeviceQrView
import com.github.diogocerqueiralima.presentation.errors.CommonError
import com.github.diogocerqueiralima.presentation.errors.Error
import com.github.diogocerqueiralima.presentation.errors.message as commonErrorMessage
import com.github.diogocerqueiralima.presentation.ui.components.HeaderComponent
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

/**
 * Resolves the message to display for a device creation error reason.
 */
@Composable
private fun Error.message(): String = when (this) {
    is CommonError -> commonErrorMessage()
    CreateDeviceError.CAMERA_PERMISSION_DENIED -> stringResource(R.string.scan_device_qr_camera_permission_denied)
    CreateDeviceError.INVALID_QR_CODE -> stringResource(R.string.scan_device_qr_invalid_code)
    CreateDeviceError.QR_PROCESSING_FAILED -> stringResource(R.string.create_device_qr_processing_failed)
    else -> stringResource(R.string.error_unexpected)
}

/**
 * This screen is responsible for collecting device details and submitting them for creation.
 *
 * @param viewModel The view model holding the device creation state.
 * @param onDeviceCreated Callback invoked once the device has been successfully created.
 * @param onBack Callback invoked when the user requests to leave the screen.
 */
@Composable
fun CreateDeviceScreen(
    viewModel: CreateDeviceViewModel,
    onDeviceCreated: (Device) -> Unit = {},
    onBack: () -> Unit = {}
) {

    val state = viewModel.state.collectAsState().value

    VehicleTrackerMobileTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                HeaderComponent(
                    icon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    title = stringResource(R.string.create_device_title),
                    description = stringResource(R.string.create_device_subtitle)
                )
            }
        ) { innerPadding ->

            when (state) {

                is CreateDeviceState.Idle -> {
                    IdleView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                is CreateDeviceState.Scanning -> {
                    ScanDeviceQrView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        cameraProvider = state.cameraProvider,
                        processImage = viewModel::processImage
                    )
                }

                is CreateDeviceState.Submitting -> {
                    CreateDeviceSubmittingView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                is CreateDeviceState.Error -> {
                    CreateDeviceErrorView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        message = state.reason.message()
                    )
                }

                is CreateDeviceState.Filling -> {
                    CreateDeviceView(
                        modifier = Modifier.padding(innerPadding),
                        id = state.form.id,
                        serialNumber = state.form.serialNumber,
                        onSerialNumberChange = viewModel::onSerialNumberChange,
                        model = state.form.model,
                        onModelChange = viewModel::onModelChange,
                        manufacturer = state.form.manufacturer,
                        onManufacturerChange = viewModel::onManufacturerChange,
                        imei = state.form.imei,
                        onImeiChange = viewModel::onImeiChange,
                        isValid = state.form.isValid,
                        onSubmit = { viewModel.createDevice(onDeviceCreated) }
                    )
                }

                is CreateDeviceState.Success -> {
                    CreateDeviceSuccessView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        device = state.device
                    )
                }

            }

        }
    }

}
