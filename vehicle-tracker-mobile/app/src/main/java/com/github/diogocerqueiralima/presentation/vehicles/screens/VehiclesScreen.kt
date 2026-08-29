package com.github.diogocerqueiralima.presentation.vehicles.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.github.diogocerqueiralima.domain.vehicles.model.Vehicle
import com.github.diogocerqueiralima.domain.vehicles.repositories.VehicleRepository
import com.github.diogocerqueiralima.domain.vehicles.services.VehicleService
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationBar
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationDestination
import com.github.diogocerqueiralima.presentation.ui.components.HeaderComponent
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme
import com.github.diogocerqueiralima.presentation.vehicles.viewmodel.VehiclesState
import com.github.diogocerqueiralima.presentation.vehicles.viewmodel.VehiclesViewModel
import com.github.diogocerqueiralima.presentation.vehicles.views.VehiclesErrorView
import com.github.diogocerqueiralima.presentation.vehicles.views.VehiclesLoadingView
import com.github.diogocerqueiralima.presentation.vehicles.views.VehiclesView
import java.time.LocalDate

/**
 * This screen is responsible for displaying a list of vehicles.
 * It includes a header, a bottom navigation bar, and the vehicles view.
 *
 * @param viewModel The view model holding the vehicles state.
 * @param onVehicleClick Callback to be invoked when a vehicle is clicked.
 * @param onNavigate Callback to be invoked when a bottom navigation destination is selected.
 */
@Composable
fun VehiclesScreen(
    viewModel: VehiclesViewModel,
    onVehicleClick: (Vehicle) -> Unit = {},
    onNavigate: (BottomNavigationDestination) -> Unit = {}
) {

    val state = viewModel.state.collectAsState().value
    val vehicles = (state as? VehiclesState.Loaded)?.vehicles ?: emptyList()

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
                    title = stringResource(R.string.list_vehicles_title),
                    description = pluralStringResource(
                        id = R.plurals.list_vehicles_subtitle,
                        count = vehicles.size,
                        formatArgs = arrayOf(vehicles.size)
                    )
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedDestination = BottomNavigationDestination.Vehicles,
                    onDestinationSelected = onNavigate
                )
            }
        ) { innerPadding ->

            when (state) {

                is VehiclesState.Loading -> {
                    VehiclesLoadingView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }

                is VehiclesState.Error -> {
                    VehiclesErrorView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        message = state.message
                    )
                }

                is VehiclesState.Loaded -> {
                    VehiclesView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        vehicles = state.vehicles,
                        onVehicleClick = onVehicleClick
                    )
                }

            }

        }
    }

}

private class PreviewVehicleRepository : VehicleRepository {
    override suspend fun findById(id: java.util.UUID): Vehicle? = null
    override suspend fun findAll(): List<Vehicle> = emptyList()
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VehiclesScreenPreview() {
    VehiclesScreen(
        viewModel = VehiclesViewModel(
            initialState = VehiclesState.Loaded(
                vehicles = listOf(
                    Vehicle(
                        vin = "VF3ABCDEF12345678",
                        plate = "AB-12-CD",
                        model = "208",
                        manufacturer = "Peugeot",
                        manufacturingDate = LocalDate.of(2022, 1, 15)
                    )
                )
            ),
            vehicleService = VehicleService(PreviewVehicleRepository())
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VehiclesScreenLoadingPreview() {
    VehiclesScreen(
        viewModel = VehiclesViewModel(
            initialState = VehiclesState.Loading,
            vehicleService = VehicleService(PreviewVehicleRepository())
        )
    )
}
