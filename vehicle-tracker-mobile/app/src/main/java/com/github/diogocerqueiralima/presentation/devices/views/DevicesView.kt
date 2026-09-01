package com.github.diogocerqueiralima.presentation.devices.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.presentation.ui.indicators.ErrorIndicator
import com.github.diogocerqueiralima.presentation.ui.indicators.LoadingIndicator
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme
import com.github.diogocerqueiralima.presentation.ui.views.InformationView
import java.util.UUID
import kotlin.time.Instant

/**
 * This view is responsible for displaying a list of devices.
 * In case the list is empty, it will show a placeholder.
 *
 * @param modifier Modifier to be applied to the view.
 * @param devices List of devices to be displayed.
 * @param onDeviceClick Callback to be invoked when a device is clicked.
 */
@Composable
fun DevicesView(
    modifier: Modifier = Modifier,
    devices: List<Device>,
    onDeviceClick: (Device) -> Unit = {}
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) {

        if (devices.isEmpty()) {
            EmptyDevicesPlaceholder(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(devices) { device ->
                    DeviceCard(
                        device = device,
                        onClick = { onDeviceClick(device) }
                    )
                }
            }
        }

    }

}

/**
 * This composable function represents a card that displays information about a device.
 *
 * @param device The device to be displayed in the card.
 * @param onClick Callback to be invoked when the card is clicked.
 */
@Composable
private fun DeviceCard(device: Device, onClick: () -> Unit = {}) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {

        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )

            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                Text(
                    text = device.displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = device.serialNumber,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )

            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

        }

    }

}

/**
 * This composable function represents a placeholder that is displayed when the list of devices is empty.
 *
 * @param modifier Modifier to be applied to the placeholder.
 */
@Composable
private fun EmptyDevicesPlaceholder(modifier: Modifier = Modifier) {

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Default.Devices,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = stringResource(R.string.list_devices_empty_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = stringResource(R.string.list_devices_empty_subtitle),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )

    }

}

/**
 * This composable function represents a loading indicator displayed while the devices are being fetched.
 *
 * @param modifier Modifier to be applied to the view.
 */
@Composable
fun DevicesLoadingView(modifier: Modifier = Modifier) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.list_devices_loading_title),
        subtitle = stringResource(R.string.list_devices_loading_subtitle),
        indicator = { LoadingIndicator() }
    )
}

/**
 * This composable function represents an error message displayed when the devices could not be retrieved.
 *
 * @param modifier Modifier to be applied to the view.
 * @param message The error message to be displayed.
 */
@Composable
fun DevicesErrorView(modifier: Modifier = Modifier, message: String) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.list_devices_error_title),
        subtitle = message,
        indicator = { ErrorIndicator() }
    )
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DevicesViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DevicesView(
                modifier = Modifier.padding(innerPadding),
                devices = listOf(
                    Device(
                        id = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                        createdAt = Instant.parse("2024-01-15T10:30:00Z"),
                        updatedAt = Instant.parse("2024-06-01T08:00:00Z"),
                        serialNumber = "SN-00123456",
                        model = "TrackPro X200",
                        manufacturer = "Teltonika",
                        imei = "352099001761481"
                    ),
                    Device(
                        id = UUID.fromString("5c48f8b0-2d3e-4f1a-9c7b-1a2b3c4d5e6f"),
                        createdAt = Instant.parse("2024-02-10T09:00:00Z"),
                        updatedAt = Instant.parse("2024-05-20T11:15:00Z"),
                        serialNumber = "SN-00123457",
                        model = "TrackPro X200",
                        manufacturer = "Teltonika",
                        imei = "352099001761482"
                    ),
                    Device(
                        id = UUID.fromString("8e0f1a2b-3c4d-4e5f-8071-2b3c4d5e6f70"),
                        createdAt = Instant.parse("2023-11-05T14:45:00Z"),
                        updatedAt = Instant.parse("2024-03-01T16:30:00Z"),
                        serialNumber = "SN-00987654",
                        model = "FMB920",
                        manufacturer = "Teltonika",
                        imei = "352099001761499"
                    )
                )
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DevicesViewEmptyPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DevicesView(
                modifier = Modifier.padding(innerPadding),
                devices = emptyList()
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DevicesLoadingViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DevicesLoadingView(modifier = Modifier.fillMaxSize().padding(innerPadding))
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DevicesErrorViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            DevicesErrorView(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                message = "Failed to load devices."
            )
        }
    }
}
