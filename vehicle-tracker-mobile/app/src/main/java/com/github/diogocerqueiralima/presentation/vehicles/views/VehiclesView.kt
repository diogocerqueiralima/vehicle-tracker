package com.github.diogocerqueiralima.presentation.vehicles.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.github.diogocerqueiralima.domain.model.Vehicle
import com.github.diogocerqueiralima.presentation.ui.components.HeaderComponent
import com.github.diogocerqueiralima.presentation.ui.icons.LocationIcon
import com.github.diogocerqueiralima.presentation.ui.theme.VehicleTrackerMobileTheme

@Composable
fun VehiclesView(
    modifier: Modifier = Modifier,
    vehicles: List<Vehicle>
) {

    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background),
    ) {

        HeaderComponent(
            icon = { LocationIcon() },
            title = "Os meus veículos",
            description = "${vehicles.size} veículos"
        )

        Text(text = "Olá")

    }

}

@Composable
private fun VehicleCard(vehicle: Vehicle) {

}

@Composable
@Preview(showBackground = true)
fun VehiclesViewPreview() {
    VehicleTrackerMobileTheme {
        VehiclesView(vehicles = emptyList())
    }
}