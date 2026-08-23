package com.github.diogocerqueiralima.presentation.welcome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.github.diogocerqueiralima.presentation.authentication.AuthenticationActivity
import com.github.diogocerqueiralima.presentation.welcome.screens.WelcomeScreen

/**
 * Activity that displays the welcome screen and handles navigation to the authentication screen.
 */
class WelcomeActivity : ComponentActivity() {

    val authenticationIntent by lazy {
        Intent(this, AuthenticationActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WelcomeScreen { startActivity(authenticationIntent) }
        }
    }

}

