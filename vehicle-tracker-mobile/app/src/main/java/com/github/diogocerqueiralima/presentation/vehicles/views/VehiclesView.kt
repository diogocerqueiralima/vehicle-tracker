package com.github.diogocerqueiralima.presentation.vehicles.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.diogocerqueiralima.R
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
            title = stringResource(R.string.list_vehicles_title),
            description = pluralStringResource(
                id = R.plurals.list_vehicles_subtitle,
                count = vehicles.size,
                formatArgs = arrayOf(vehicles.size)
            )
        )

        LazyColumn() {
            items(vehicles) { vehicle ->
                VehicleCard(vehicle)
            }
        }

    }

}

@Composable
private fun VehicleCard(vehicle: Vehicle) {

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        shape = RoundedCornerShape(6.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(8.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Column() {

                        Text(
                            text = vehicle.name
                        )

                        Text(
                            text = vehicle.plate
                        )
                    }

                    Text(
                        text = "Cacém, Lisboa"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text = "45 km/h"
                        )

                        Text(
                            text = "73%"
                        )

                    }

                }

                Text(
                    text = "Em movimento"
                )

            }

        }

    }

}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun VehiclesViewPreview() {
    VehicleTrackerMobileTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            VehiclesView(
                modifier = Modifier.padding(innerPadding),
                vehicles = listOf(
                    Vehicle(name = "Peugeot 2008", plate = "AA-AA-AA"),
                    Vehicle(name = "Peugeot 208", plate = "BB-BB-BB"),
                    Vehicle(name = "Mercedes Benz", plate = "CC-CC-CC")
                )
            )
        }
    }
}