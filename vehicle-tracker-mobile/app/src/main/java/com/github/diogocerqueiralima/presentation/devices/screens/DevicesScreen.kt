package com.github.diogocerqueiralima.presentation.devices.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.domain.devices.model.Device
import com.github.diogocerqueiralima.domain.devices.repositories.DeviceRepository
import com.github.diogocerqueiralima.domain.devices.services.DeviceService
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationBar
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationDestination
import com.github.diogocerqueiralima.presentation.ui.components.HeaderComponent
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme
import com.github.diogocerqueiralima.presentation.devices.viewmodel.DevicesState
import com.github.diogocerqueiralima.presentation.devices.viewmodel.DevicesViewModel
import com.github.diogocerqueiralima.presentation.devices.views.DevicesErrorView
import com.github.diogocerqueiralima.presentation.devices.views.DevicesLoadingView
import com.github.diogocerqueiralima.presentation.devices.views.DevicesView
import kotlin.time.Instant

/**
 * This screen is responsible for displaying a list of devices.
 * It includes a header, a bottom navigation bar, and the devices view.
 *
 * @param viewModel The view model holding the devices state.
 * @param onDeviceClick Callback to be invoked when a device is clicked.
 * @param onAddDevice Callback to be invoked when the user wants to create a new device.
 * @param onNavigate Callback to be invoked when a bottom navigation destination is selected.
 */
@Composable
fun DevicesScreen(
    viewModel: DevicesViewModel,
    onDeviceClick: (Device) -> Unit = {},
    onAddDevice: () -> Unit = {},
    onNavigate: (BottomNavigationDestination) -> Unit = {}
) {

    val state = viewModel.state.collectAsState().value
    val devices = (state as? DevicesState.Loaded)?.devices ?: emptyList()

    VehicleTrackerMobileTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                HeaderComponent(
                    icon = {
                        Image(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    },
                    title = stringResource(R.string.list_devices_title),
                    description = pluralStringResource(
                        id = R.plurals.list_devices_subtitle,
                        count = devices.size,
                        formatArgs = arrayOf(devices.size)
                    )
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedDestination = BottomNavigationDestination.Devices,
                    onDestinationSelected = onNavigate
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddDevice) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        ) { innerPadding ->

            when (state) {

                is DevicesState.Loading -> {
                    DevicesLoadingView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                is DevicesState.Error -> {
                    DevicesErrorView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        message = state.message
                    )
                }

                is DevicesState.Loaded -> {
                    DevicesView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        devices = state.devices,
                        onDeviceClick = onDeviceClick
                    )
                }

            }

        }
    }

}

private class PreviewDeviceRepository : DeviceRepository {
    override suspend fun findById(id: java.util.UUID): Device? = null
    override suspend fun findAll(): List<Device> = emptyList()
    override suspend fun createOrUpdate(id: java.util.UUID, serialNumber: String, model: String, manufacturer: String, imei: String, ownerId: java.util.UUID): Device =
        throw NotImplementedError()
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DevicesScreenPreview() {
    DevicesScreen(
        viewModel = DevicesViewModel(
            initialState = DevicesState.Loaded(
                devices = listOf(
                    Device(
                        id = java.util.UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                        createdAt = Instant.parse("2024-01-15T10:30:00Z"),
                        updatedAt = Instant.parse("2024-06-01T08:00:00Z"),
                        serialNumber = "SN-00123456",
                        model = "TrackPro X200",
                        manufacturer = "Teltonika",
                        imei = "352099001761481"
                    )
                )
            ),
            deviceService = DeviceService(PreviewDeviceRepository())
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DevicesScreenLoadingPreview() {
    DevicesScreen(
        viewModel = DevicesViewModel(
            initialState = DevicesState.Loading,
            deviceService = DeviceService(PreviewDeviceRepository())
        )
    )
}
