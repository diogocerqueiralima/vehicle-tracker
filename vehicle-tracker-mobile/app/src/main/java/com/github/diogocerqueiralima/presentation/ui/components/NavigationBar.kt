package com.github.diogocerqueiralima.presentation.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.github.diogocerqueiralima.R
import com.github.diogocerqueiralima.presentation.home.HomeActivity
import com.github.diogocerqueiralima.presentation.vehicles.VehiclesActivity
import kotlin.reflect.KClass

enum class BottomNavigationDestination(
    val labelRes: Int,
    val icon: ImageVector,
    val activityClass: KClass<out ComponentActivity>
) {
    Home(labelRes = R.string.nav_home, icon = Icons.Filled.Home, activityClass = HomeActivity::class),

    Vehicles(labelRes = R.string.nav_vehicles, icon = Icons.Filled.DirectionsCar, activityClass = VehiclesActivity::class),

    Devices(labelRes = R.string.nav_devices, icon = Icons.Filled.Devices, activityClass = VehiclesActivity::class) // Replace with actual DevicesActivity when available
}

@Composable
fun BottomNavigationBar(
    selectedDestination: BottomNavigationDestination,
    onDestinationSelected: (BottomNavigationDestination) -> Unit
) {

    NavigationBar {

        BottomNavigationDestination.entries.forEach { destination ->

            NavigationBarItem(
                selected = destination == selectedDestination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(destination.labelRes)
                    )
                },
                label = {
                    Text(text = stringResource(destination.labelRes))
                }
            )

        }

    }

}
