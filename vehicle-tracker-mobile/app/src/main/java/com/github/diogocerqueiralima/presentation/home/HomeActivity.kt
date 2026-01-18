package com.github.diogocerqueiralima.presentation.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.github.diogocerqueiralima.presentation.authentication.AuthenticationActivity
import com.github.diogocerqueiralima.presentation.home.screens.HomeScreen

class HomeActivity : ComponentActivity() {

    val authenticationIntent by lazy {
        Intent(this, AuthenticationActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeScreen { startActivity(authenticationIntent) }
        }
    }

}

