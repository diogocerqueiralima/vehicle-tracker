package com.github.diogocerqueiralima.presentation.authentication.viewmodel

import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.diogocerqueiralima.BuildConfig
import com.github.diogocerqueiralima.domain.services.TokenService
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.AuthenticationState.Authenticating
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.AuthenticationState.Error
import com.github.diogocerqueiralima.presentation.authentication.viewmodel.AuthenticationState.Idle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

const val TAG = "AUTHENTICATION_VIEW_MODEL"

/**
 * Represents the different states of the authentication process.
 * This sealed interface defines the possible states during the OAuth2 authentication flow.
 */
sealed interface AuthenticationState {
    data object Idle : AuthenticationState
    data class Authenticating(val state: String, val codeVerifier: String) : AuthenticationState
    data object Authenticated : AuthenticationState
    data class Error(val message: String) : AuthenticationState
}

class AuthenticationViewModel(
    initialState: AuthenticationState = Idle,
    val tokenService: TokenService
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

        _state.value = Authenticating(
            state = state,
            codeVerifier = codeVerifier
        )

        openAuthorizationUriIntent(authorizationUri)
    }

    /**
     * Handles authentication errors that may occur during the OAuth2 flow.
     * This method updates the state to Error with the provided error message.
     *
     * @param error The error message received during authentication, if any.
     */
    fun handleAuthenticationError(error: String?) {

        if (_state.value !is Authenticating) return

        _state.value = Error(
            message = error ?: "An unexpected error occurred during authentication."
        )

    }

    /**
     * Handles the authorization code received from the OAuth2 flow.
     * This method checks the state parameter and retrieves the code verifier
     *
     * @param code The authorization code received from the OAuth2 provider.
     * @param state The state parameter received from the OAuth2 provider.
     */
    fun handleAuthorizationCode(code: String, state: String) {

        val currentState = _state.value
        Log.d(TAG, "Received authorization code: $code with state: $state, current state: $currentState")

        if (currentState !is Authenticating) return

        val expectedState = currentState.state

        if (state != expectedState) {
            _state.value = Error(
                message = "State parameter does not match. You may be a victim of an attack."
            )
            return
        }

        val codeVerifier = currentState.codeVerifier

        viewModelScope.launch {
            tokenService.getTokenAndSave(authorizationCode = code, codeVerifier = codeVerifier)
        }

    }

    /**
     * Generates a secure code verifier for PKCE.
     * This method creates a random string that will be used as the code verifier
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
class AuthenticationViewModelFactory(val tokenService: TokenService) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthenticationViewModel(tokenService = tokenService) as T
    }

}