package com.github.diogocerqueiralima.presentation.authentication.viewmodel

import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.diogocerqueiralima.BuildConfig
import com.github.diogocerqueiralima.domain.services.AuthenticationService
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.AuthenticationState.Authenticating
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.AuthenticationState.Idle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

/**
 * Represents the different states of the authentication process.
 * This sealed interface defines the possible states during the OAuth2 authentication flow.
 */
sealed interface AuthenticationState {
    data object Idle : AuthenticationState
    data object Authenticating : AuthenticationState
    data object Canceled: AuthenticationState
}

class AuthenticationViewModel(
    initialState: AuthenticationState,
    val authenticationService: AuthenticationService
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<AuthenticationState> = _state.asStateFlow()

    /**
     * Starts the authentication process.
     * This process starts the OAuth2 flow to authenticate the user.
     *
     * @param openAuthorizationUriIntent A function that takes a Uri and opens it in a custom tab.
     */
    fun startAuthentication(openAuthorizationUriIntent: (uri: Uri) -> Unit) {

        if (_state.value !is Idle) return

        val state = UUID.randomUUID().toString()
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)
        val authorizationUri = BuildConfig.AUTHORIZATION_URI.toUri()
            .buildUpon()
            .appendQueryParameter("client_id", BuildConfig.CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", BuildConfig.REDIRECT_URI)
            .appendQueryParameter("scope", "openid profile email")
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()

        viewModelScope.launch {
            authenticationService.prepareRedirect(codeVerifier, state)
            openAuthorizationUriIntent(authorizationUri)
            _state.value = Authenticating
        }

    }

    /**
     * <p>Handles authentication cancellation by the user.
     * <p>This can happen if the user closes the authentication tab or presses the back button
     * during the authentication process.
     *
     * @param homeIntent A function that redirects the user to the home screen, used when authentication is canceled.
     */
    fun handleAuthenticationCancellation(homeIntent: () -> Unit) {

        if (_state.value !is Authenticating) return
        _state.value = AuthenticationState.Canceled

        homeIntent()
    }

    /**
     * Generates a secure code verifier for PKCE.
     * This method creates a random string that will be used as the code verifier
     *
     * @return A securely generated code verifier string that can be used in the PKCE flow.
     */
    private fun generateCodeVerifier(): String {

        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)

        return Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    /**
     * Generates a code challenge from the given code verifier using SHA-256.
     * This method creates a hashed version of the code verifier to be sent to the OAuth2 provider.
     *
     * @param codeVerifier The code verifier for which to generate the code challenge.
     * @return A code challenge string derived from the code verifier, suitable for use in the PKCE flow.
     */
    private fun generateCodeChallenge(codeVerifier: String): String {

        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(codeVerifier.toByteArray())

        return Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

}

@Suppress("UNCHECKED_CAST")
class AuthenticationViewModelFactory(
    val authenticationService: AuthenticationService,
    val initialState: AuthenticationState = Idle
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthenticationViewModel(
            authenticationService = authenticationService,
            initialState = initialState
        ) as T
    }

}