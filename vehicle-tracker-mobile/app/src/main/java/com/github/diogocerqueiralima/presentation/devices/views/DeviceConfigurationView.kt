package com.github.diogocerqueiralima.presentation.devices.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.presentation.ui.indicators.ErrorIndicator
import com.github.diogocerqueiralima.presentation.ui.indicators.LoadingIndicator
import com.github.diogocerqueiralima.presentation.ui.indicators.SuccessIndicator
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme
import com.github.diogocerqueiralima.presentation.ui.views.InformationView
import java.util.UUID
import kotlin.time.Instant

/**
 * This view is displayed while the device configuration flow is starting up, before a device has
 * been given to connect to.
 *
 * @param modifier Modifier to be applied to the view.
 */
@Composable
fun DeviceConfigurationIdleView(modifier: Modifier = Modifier) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.getting_ready_title),
        subtitle = stringResource(R.string.please_wait_subtitle),
        indicator = { LoadingIndicator() }
    )
}

/**
 * This view is displayed while the device is being connected to.
 *
 * @param modifier Modifier to be applied to the view.
 */
@Composable
fun DeviceConfigurationConnectingView(modifier: Modifier = Modifier) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.device_configuration_connecting_title),
        subtitle = stringResource(R.string.device_configuration_connecting_subtitle),
        indicator = { LoadingIndicator() }
    )
}

/**
 * This view is displayed once the device has been successfully connected to.
 *
 * @param modifier Modifier to be applied to the view.
 * @param device The device that was connected to.
 */
@Composable
fun DeviceConfigurationConnectedView(modifier: Modifier = Modifier, device: Device) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.device_configuration_connected_title),
        subtitle = device.displayName,
        indicator = { SuccessIndicator() }
    )
}

/**
 * This view is displayed when an error occurs during the device configuration flow.
 *
 * @param modifier Modifier to be applied to the view.
 * @param message Error message describing what went wrong.
 */
@Composable
fun DeviceConfigurationErrorView(modifier: Modifier = Modifier, message: String) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.device_configuration_error_title),
        subtitle = message,
        indicator = { ErrorIndicator() }
    )
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DeviceConfigurationIdleViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DeviceConfigurationIdleView(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DeviceConfigurationConnectingViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DeviceConfigurationConnectingView(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DeviceConfigurationConnectedViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DeviceConfigurationConnectedView(
                modifier = Modifier.padding(innerPadding),
                device = Device(
                    id = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                    createdAt = Instant.parse("2024-01-15T10:30:00Z"),
                    updatedAt = Instant.parse("2024-06-01T08:00:00Z"),
                    serialNumber = "SN-00123456",
                    model = "TrackPro X200",
                    manufacturer = "Teltonika",
                    imei = "352099001761481"
                )
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DeviceConfigurationErrorViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DeviceConfigurationErrorView(
                modifier = Modifier.padding(innerPadding),
                message = "Failed to connect to the device."
            )
        }
    }
}
