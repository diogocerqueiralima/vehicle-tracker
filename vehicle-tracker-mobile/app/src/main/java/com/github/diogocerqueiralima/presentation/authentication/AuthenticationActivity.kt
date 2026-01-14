package com.github.diogocerqueiralima.presentation.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import com.github.diogocerqueiralima.DependenciesContainer
import com.github.diogocerqueiralima.domain.services.AuthenticationService
import com.github.diogocerqueiralima.infrastructure.http.AuthenticationHttpClient
import com.github.diogocerqueiralima.presentation.authentication.screens.AuthenticationScreen
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.AuthenticationViewModel
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.AuthenticationViewModelFactory

const val TAG = "AUTHENTICATION_ACTIVITY"

class AuthenticationActivity : ComponentActivity() {

    private val viewModel by viewModels<AuthenticationViewModel>(
        factoryProducer = {

            val dependenciesContainer = application as DependenciesContainer
            val client = AuthenticationHttpClient(dependenciesContainer.httpClient)

            AuthenticationViewModelFactory(authenticationService = AuthenticationService(client))
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleRedirect(intent)
    }

    /**
     *
     * Handles the redirect intent from the OAuth2 authentication flow.
     * This method extracts the authorization code from the intent and
     * passes it to the ViewModel for further processing.
     *
     * If the intent does not contain the expected data, this method will not perform any action.
     *
     * @param intent The intent received in onNewIntent containing the redirect data.
     */
    private fun handleRedirect(intent: Intent) {

        val uri = intent.data ?: return

        if (uri.scheme != "mytracker" || uri.host != "oauth" || uri.path != "/callback") {
            return
        }

        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        val error = uri.getQueryParameter("error")

        if (error != null || code == null || state == null) {

            // if there's an error or missing parameters, log and handle it
            Log.e(TAG, "Authentication error: $error")
            viewModel.handleAuthenticationError(error)

        } else {

            // pass the code and state to the ViewModel for token exchange
            viewModel.handleAuthorizationCode(code, state)

        }

    }

}