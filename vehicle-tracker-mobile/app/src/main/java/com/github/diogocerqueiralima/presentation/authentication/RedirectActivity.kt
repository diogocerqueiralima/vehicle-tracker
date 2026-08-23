package com.github.diogocerqueiralima.presentation.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.github.diogocerqueiralima.DependenciesContainer
import com.github.diogocerqueiralima.presentation.authentication.screens.RedirectScreen
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.RedirectViewModel
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.RedirectViewModelFactory
import com.github.diogocerqueiralima.presentation.home.HomeActivity
import com.github.diogocerqueiralima.presentation.welcome.WelcomeActivity

const val TAG = "REDIRECT_ACTIVITY"

/**
 * Activity that handles the redirect from the authentication flow, processes the authorization code or error, and navigates to the appropriate screen.
 */
class RedirectActivity : ComponentActivity() {

    val homeIntent by lazy {
        Intent(this, HomeActivity::class.java)
    }

    val welcomeIntent by lazy {
        Intent(this, WelcomeActivity::class.java)
    }

    private val viewModel by viewModels<RedirectViewModel>(
        factoryProducer = {

            val dependenciesContainer = application as DependenciesContainer
            RedirectViewModelFactory(dependenciesContainer.authenticationService)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RedirectScreen(viewModel)
        }

        Log.d(TAG, "RedirectActivity created with intent: $intent")
        val uri = intent?.data ?: return

        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        val error = uri.getQueryParameter("error")

        Log.d(TAG, "Received URI: $uri")
        Log.d(TAG, "Extracted code: $code, state: $state, error: $error")

        if (code != null && state != null) {
            viewModel.handleAuthorizationCode(
                code = code,
                state = state,
                welcomeIntent = { startActivity(welcomeIntent) },
                homeIntent = { startActivity(homeIntent) }
            )
            return
        }

        viewModel.handleAuthenticationError(error ?: "Unknown error during authentication.") {
            startActivity(welcomeIntent)
        }
    }

}