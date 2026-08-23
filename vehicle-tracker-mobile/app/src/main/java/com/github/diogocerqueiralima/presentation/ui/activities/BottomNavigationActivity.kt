package com.github.diogocerqueiralima.presentation.ui.activities

import android.content.Intent
import androidx.activity.ComponentActivity
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationDestination

abstract class BottomNavigationActivity(
    private val currentDestination: BottomNavigationDestination
) : ComponentActivity() {

    protected fun navigateTo(destination: BottomNavigationDestination) {
        if (destination == currentDestination) return
        startActivity(Intent(this, destination.activityClass.java))
    }

}
