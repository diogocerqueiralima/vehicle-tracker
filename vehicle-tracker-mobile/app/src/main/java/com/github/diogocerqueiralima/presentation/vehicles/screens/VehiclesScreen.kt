package com.github.diogocerqueiralima.presentation.vehicles.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.domain.model.Vehicle
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationBar
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationDestination
import com.github.diogocerqueiralima.presentation.ui.components.HeaderComponent
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme
import com.github.diogocerqueiralima.presentation.vehicles.views.VehiclesView

/**
 * This screen is responsible for displaying a list of vehicles.
 * It includes a header, a bottom navigation bar, and the vehicles view.
 *
 * @param vehicles List of vehicles to be displayed.
 * @param onVehicleClick Callback to be invoked when a vehicle is clicked.
 * @param onNavigate Callback to be invoked when a bottom navigation destination is selected.
 */
@Composable
fun VehiclesScreen(
    vehicles: List<Vehicle> = emptyList(),
    onVehicleClick: (Vehicle) -> Unit = {},
    onNavigate: (BottomNavigationDestination) -> Unit = {}
) {

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
            VehiclesView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                vehicles = vehicles,
                onVehicleClick = onVehicleClick
            )
        }
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VehiclesScreenPreview() {
    VehiclesScreen(
        vehicles = listOf(
            Vehicle(
                name = "My Car",
                plate = "AB-12-CD"
            )
        )
    )
}
