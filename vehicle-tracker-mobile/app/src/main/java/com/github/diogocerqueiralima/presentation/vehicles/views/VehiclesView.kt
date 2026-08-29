package com.github.diogocerqueiralima.presentation.vehicles.views

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
import androidx.compose.material.icons.filled.DirectionsCar
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
import com.github.diogocerqueiralima.domain.vehicles.model.Vehicle
import com.github.diogocerqueiralima.presentation.ui.indicators.ErrorIndicator
import com.github.diogocerqueiralima.presentation.ui.indicators.LoadingIndicator
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme
import com.github.diogocerqueiralima.presentation.ui.views.InformationView
import java.time.LocalDate

/**
 * This view is responsible for displaying a list of vehicles.
 * In case the list is empty, it will show a placeholder.
 *
 * @param modifier Modifier to be applied to the view.
 * @param vehicles List of vehicles to be displayed.
 * @param onVehicleClick Callback to be invoked when a vehicle is clicked.
 */
@Composable
fun VehiclesView(
    modifier: Modifier = Modifier,
    vehicles: List<Vehicle>,
    onVehicleClick: (Vehicle) -> Unit = {}
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) {

        if (vehicles.isEmpty()) {
            EmptyVehiclesPlaceholder(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vehicles) { vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        onClick = { onVehicleClick(vehicle) }
                    )
                }
            }
        }

    }

}

/**
 * This composable function represents a card that displays information about a vehicle.
 *
 * @param vehicle The vehicle to be displayed in the card.
 * @param onClick Callback to be invoked when the card is clicked.
 */
@Composable
private fun VehicleCard(vehicle: Vehicle, onClick: () -> Unit = {}) {

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
                    imageVector = Icons.Default.DirectionsCar,
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
                    text = vehicle.displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = vehicle.plate,
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
 * This composable function represents a placeholder that is displayed when the list of vehicles is empty.
 *
 * @param modifier Modifier to be applied to the placeholder.
 */
@Composable
private fun EmptyVehiclesPlaceholder(modifier: Modifier = Modifier) {

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = stringResource(R.string.list_vehicles_empty_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = stringResource(R.string.list_vehicles_empty_subtitle),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )

    }

}

/**
 * This composable function represents a loading indicator displayed while the vehicles are being fetched.
 *
 * @param modifier Modifier to be applied to the view.
 */
@Composable
fun VehiclesLoadingView(modifier: Modifier = Modifier) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.list_vehicles_loading_title),
        subtitle = stringResource(R.string.list_vehicles_loading_subtitle),
        indicator = { LoadingIndicator() }
    )
}

/**
 * This composable function represents an error message displayed when the vehicles could not be retrieved.
 *
 * @param modifier Modifier to be applied to the view.
 * @param message The error message to be displayed.
 */
@Composable
fun VehiclesErrorView(modifier: Modifier = Modifier, message: String) {
    InformationView(
        modifier = modifier,
        title = stringResource(R.string.list_vehicles_error_title),
        subtitle = message,
        indicator = { ErrorIndicator() }
    )
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun VehiclesViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            VehiclesView(
                modifier = Modifier.padding(innerPadding),
                vehicles = listOf(
                    Vehicle(
                        vin = "VF3ABCDEF12345678",
                        plate = "AA-AA-AA",
                        model = "2008",
                        manufacturer = "Peugeot",
                        manufacturingDate = LocalDate.of(2022, 1, 15)
                    ),
                    Vehicle(
                        vin = "VF3ABCDEF12345679",
                        plate = "BB-BB-BB",
                        model = "208",
                        manufacturer = "Peugeot",
                        manufacturingDate = LocalDate.of(2021, 6, 10)
                    ),
                    Vehicle(
                        vin = "WDB1234561A123456",
                        plate = "CC-CC-CC",
                        model = "C-Class",
                        manufacturer = "Mercedes Benz",
                        manufacturingDate = LocalDate.of(2020, 3, 5)
                    )
                )
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun VehiclesViewEmptyPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            VehiclesView(
                modifier = Modifier.padding(innerPadding),
                vehicles = emptyList()
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun VehiclesLoadingViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            VehiclesLoadingView(modifier = Modifier.fillMaxSize().padding(innerPadding))
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun VehiclesErrorViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            VehiclesErrorView(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                message = "Failed to load vehicles."
            )
        }
    }
}