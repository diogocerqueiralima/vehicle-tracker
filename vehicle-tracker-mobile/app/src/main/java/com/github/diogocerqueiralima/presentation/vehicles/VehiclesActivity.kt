package com.github.diogocerqueiralima.presentation.vehicles

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.github.diogocerqueiralima.presentation.ui.activities.BottomNavigationActivity
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationDestination
import com.github.diogocerqueiralima.presentation.vehicles.screens.VehiclesScreen

/**
 * Activity that displays the vehicles screen and handles navigation to other destinations.
 */
class VehiclesActivity : BottomNavigationActivity(BottomNavigationDestination.Vehicles) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VehiclesScreen(onNavigate = ::navigateTo)
        }
    }

}
