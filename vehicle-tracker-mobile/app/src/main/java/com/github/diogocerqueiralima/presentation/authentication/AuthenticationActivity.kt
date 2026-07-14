package com.github.diogocerqueiralima.presentation.authentication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import com.github.diogocerqueiralima.DependenciesContainer
import com.github.diogocerqueiralima.domain.services.AuthenticationService
import com.github.diogocerqueiralima.infrastructure.http.AuthenticationHttpClient
import com.github.diogocerqueiralima.infrastructure.repositories.KeyRepositoryImpl
import com.github.diogocerqueiralima.infrastructure.repositories.UserPreSessionRepositoryImpl
import com.github.diogocerqueiralima.infrastructure.repositories.UserSessionRepositoryImpl
import com.github.diogocerqueiralima.presentation.authentication.screens.AuthenticationScreen
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.AuthenticationViewModel
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.AuthenticationViewModelFactory

/**
 * Activity that handles the authentication process, including starting the authentication flow and handling cancellation.
 */
class AuthenticationActivity : ComponentActivity() {

    private val viewModel by viewModels<AuthenticationViewModel>(
        factoryProducer = {

            val dependenciesContainer = application as DependenciesContainer
            val client = AuthenticationHttpClient(dependenciesContainer.httpClient)
            val dataStore = dependenciesContainer.dataStore
            val keyStore = dependenciesContainer.keyStore
            val keyRepository = KeyRepositoryImpl(keyStore)
            val userSessionRepository = UserSessionRepositoryImpl(dataStore, keyRepository)
            val userPreSessionRepository = UserPreSessionRepositoryImpl(dataStore)
            val authenticationService = AuthenticationService(client, userSessionRepository, userPreSessionRepository)

            AuthenticationViewModelFactory(authenticationService)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AuthenticationScreen(viewModel)
        }

        viewModel.startAuthentication { uri ->
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(this@AuthenticationActivity, uri)
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.handleAuthenticationCancellation { finish() }
    }

}