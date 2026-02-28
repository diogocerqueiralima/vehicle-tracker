package com.github.diogocerqueiralima.presentation.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.github.diogocerqueiralima.DependenciesContainer
import com.github.diogocerqueiralima.domain.services.AuthenticationService
import com.github.diogocerqueiralima.infrastructure.http.AuthenticationHttpClient
import com.github.diogocerqueiralima.infrastructure.repositories.KeyRepositoryImpl
import com.github.diogocerqueiralima.infrastructure.repositories.UserPreSessionRepositoryImpl
import com.github.diogocerqueiralima.infrastructure.repositories.UserSessionRepositoryImpl
import com.github.diogocerqueiralima.presentation.authentication.screens.RedirectScreen
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.RedirectViewModel
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.RedirectViewModelFactory
import com.github.diogocerqueiralima.presentation.home.HomeActivity

const val TAG = "REDIRECT_ACTIVITY"

class RedirectActivity : ComponentActivity() {

    val homeIntent by lazy {
        Intent(this, HomeActivity::class.java)
    }

    private val viewModel by viewModels<RedirectViewModel>(
        factoryProducer = {

            val dependenciesContainer = application as DependenciesContainer
            val client = AuthenticationHttpClient(dependenciesContainer.httpClient)
            val dataStore = dependenciesContainer.dataStore
            val keyStore = dependenciesContainer.keyStore
            val keyRepository = KeyRepositoryImpl(keyStore)
            val userSessionRepository = UserSessionRepositoryImpl(dataStore, keyRepository)
            val userPreSessionRepository = UserPreSessionRepositoryImpl(dataStore)
            val authenticationService = AuthenticationService(client, userSessionRepository, userPreSessionRepository)

            RedirectViewModelFactory(authenticationService)
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
            viewModel.handleAuthorizationCode(code, state) {
                startActivity(homeIntent)
            }
            return
        }

        viewModel.handleAuthenticationError(error ?: "Unknown error during authentication.") {
            startActivity(homeIntent)
        }
    }

}