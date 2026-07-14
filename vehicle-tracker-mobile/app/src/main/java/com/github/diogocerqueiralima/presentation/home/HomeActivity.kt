package com.github.diogocerqueiralima.presentation.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.github.diogocerqueiralima.presentation.home.screens.HomeScreen
import com.github.diogocerqueiralima.presentation.ui.activities.BottomNavigationActivity
import com.github.diogocerqueiralima.presentation.ui.components.BottomNavigationDestination

class HomeActivity : BottomNavigationActivity(BottomNavigationDestination.Home) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeScreen(onNavigate = ::navigateTo)
        }
    }

}
