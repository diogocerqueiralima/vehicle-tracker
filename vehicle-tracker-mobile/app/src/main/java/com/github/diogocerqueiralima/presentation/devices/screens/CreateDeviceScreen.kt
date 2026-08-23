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
import com.github.diogocerqueiralima.presentation.devices.viewmodel.CreateDeviceFormState
import com.github.diogocerqueiralima.presentation.devices.viewmodel.CreateDeviceState
import com.github.diogocerqueiralima.presentation.devices.viewmodel.CreateDeviceViewModel
import com.github.diogocerqueiralima.presentation.devices.views.CreateDeviceErrorView
import com.github.diogocerqueiralima.presentation.devices.views.CreateDeviceSubmittingView
import com.github.diogocerqueiralima.presentation.devices.views.CreateDeviceSuccessView
import com.github.diogocerqueiralima.presentation.devices.views.CreateDeviceView
import com.github.diogocerqueiralima.presentation.ui.components.HeaderComponent
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

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
    val form = when (state) {
        is CreateDeviceState.Filling -> state.form
        is CreateDeviceState.Submitting -> state.form
        is CreateDeviceState.Error -> state.form
        is CreateDeviceState.Success -> CreateDeviceFormState()
    }

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

                is CreateDeviceState.Submitting -> {
                    CreateDeviceSubmittingView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                is CreateDeviceState.Error -> {
                    CreateDeviceErrorView(
                        modifier = Modifier.padding(innerPadding),
                        serialNumber = form.serialNumber,
                        onSerialNumberChange = viewModel::onSerialNumberChange,
                        model = form.model,
                        onModelChange = viewModel::onModelChange,
                        manufacturer = form.manufacturer,
                        onManufacturerChange = viewModel::onManufacturerChange,
                        imei = form.imei,
                        onImeiChange = viewModel::onImeiChange,
                        isValid = form.isValid,
                        message = state.message,
                        onSubmit = { viewModel.createDevice(onDeviceCreated) }
                    )
                }

                is CreateDeviceState.Filling -> {
                    CreateDeviceView(
                        modifier = Modifier.padding(innerPadding),
                        serialNumber = form.serialNumber,
                        onSerialNumberChange = viewModel::onSerialNumberChange,
                        model = form.model,
                        onModelChange = viewModel::onModelChange,
                        manufacturer = form.manufacturer,
                        onManufacturerChange = viewModel::onManufacturerChange,
                        imei = form.imei,
                        onImeiChange = viewModel::onImeiChange,
                        isValid = form.isValid,
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
