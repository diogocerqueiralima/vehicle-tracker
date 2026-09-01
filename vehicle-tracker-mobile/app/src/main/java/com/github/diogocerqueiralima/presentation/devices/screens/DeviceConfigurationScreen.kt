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
import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.presentation.devices.viewmodel.DeviceConfigurationReason
import com.github.diogocerqueiralima.presentation.devices.viewmodel.DeviceConfigurationState
import com.github.diogocerqueiralima.presentation.devices.viewmodel.DeviceConfigurationViewModel
import com.github.diogocerqueiralima.presentation.devices.views.DeviceConfigurationConnectedView
import com.github.diogocerqueiralima.presentation.devices.views.DeviceConfigurationConnectingView
import com.github.diogocerqueiralima.presentation.devices.views.DeviceConfigurationErrorView
import com.github.diogocerqueiralima.presentation.devices.views.DeviceConfigurationIdleView
import com.github.diogocerqueiralima.presentation.errors.CommonReason
import com.github.diogocerqueiralima.presentation.errors.Reason
import com.github.diogocerqueiralima.presentation.errors.message as commonErrorMessage
import com.github.diogocerqueiralima.presentation.ui.components.HeaderComponent
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

/**
 * Resolves the message to display for a device configuration error reason.
 */
@Composable
private fun Reason.message(): String = when (this) {
    is CommonReason -> commonErrorMessage()
    DeviceConfigurationReason.CONNECTION_FAILED -> stringResource(R.string.device_configuration_connection_failed)
    DeviceConfigurationReason.BLUETOOTH_PERMISSION_DENIED -> stringResource(R.string.device_configuration_bluetooth_permission_denied)
    else -> stringResource(R.string.error_unexpected)
}

/**
 * This screen is responsible for connecting to and configuring a device.
 *
 * @param viewModel The view model holding the device configuration state.
 * @param onBack Callback invoked when the user requests to leave the screen.
 */
@Composable
fun DeviceConfigurationScreen(viewModel: DeviceConfigurationViewModel, onBack: () -> Unit = {}) {

    val state = viewModel.state.collectAsState().value

    val device = when (state) {
        is DeviceConfigurationState.Connecting -> state.device
        is DeviceConfigurationState.Connected -> state.device
        DeviceConfigurationState.Idle, is DeviceConfigurationState.Error -> null
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
                    title = stringResource(R.string.device_configuration_header_title),
                    description = device?.displayName ?: stringResource(R.string.device_configuration_header_subtitle)
                )
            }
        ) { innerPadding ->

            when (state) {

                DeviceConfigurationState.Idle -> {
                    DeviceConfigurationIdleView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                is DeviceConfigurationState.Connecting -> {
                    DeviceConfigurationConnectingView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                is DeviceConfigurationState.Connected -> {

                    val characteristicValues = viewModel.characteristicValues.collectAsState().value

                    DeviceConfigurationConnectedView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        device = state.device,
                        characteristicValues = characteristicValues,
                        onExpandService = viewModel::readService,
                        onWriteCharacteristic = viewModel::writeCharacteristic
                    )
                }

                is DeviceConfigurationState.Error -> {
                    DeviceConfigurationErrorView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        message = state.reason.message()
                    )
                }

            }

        }
    }

}
