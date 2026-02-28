package com.github.diogocerqueiralima.presentation.authentication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.diogocerqueiralima.domain.services.AuthenticationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val REDIRECT_TAG = "REDIRECT_VIEW_MODEL"

/**
 * Represents the different states of the redirect process.
 * This sealed interface defines the possible states during the OAuth2 redirect handling.
 */
sealed interface RedirectState {
    data object Redirected : RedirectState
    data class Error(val message: String) : RedirectState
    data object Authenticated : RedirectState
}

class RedirectViewModel(
    initialState: RedirectState,
    val authenticationService: AuthenticationService
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<RedirectState> = _state.asStateFlow()

    /**
     * Handles authentication errors that may occur during the OAuth2 flow.
     * This method updates the state to Error with the provided error message.
     *
     * @param error The error message received during authentication, if any.
     * @param homeIntent A function that redirects the user to the home screen, used in case of errors.
     */
    fun handleAuthenticationError(error: String?, homeIntent: () -> Unit) {

        // 1. Ensure we are in the correct state to handle the error
        if (_state.value !is RedirectState.Redirected) return

        // 2. Update the state to Error with the provided error message or a default message
        _state.value = RedirectState.Error(
            message = error ?: "An unexpected error occurred during authentication."
        )

        // 3. Send user back to the home screen
        homeIntent()
    }

    /**
     * Handles the authorization code received from the OAuth2 flow.
     * This method checks the state parameter and retrieves the code verifier
     *
     * @param code The authorization code received from the OAuth2 provider.
     * @param state The state parameter received from the OAuth2 provider.
     * @param homeIntent A function that redirects the user to the home screen, used in case of errors.
     */
    fun handleAuthorizationCode(code: String, state: String, homeIntent: () -> Unit) {

        // 1. Get the current state
        val currentState = _state.value
        Log.d(REDIRECT_TAG, "Received authorization code: $code with state: $state, current state: $currentState")

        // 2. Ensure we are in the correct state to handle the authorization code
        if (currentState !is RedirectState.Redirected) return

        viewModelScope.launch {

            // 3. Get the pre-session data, which includes the code verifier and the original state
            val preSession = authenticationService.getPreSession() ?: return@launch

            Log.d(REDIRECT_TAG, "Retrieved pre-session data: codeVerifier=${preSession.codeVerifier}, state=${preSession.state}")

            // 4. Validate the state parameter to prevent CSRF attacks
            if (state != preSession.state) {
                _state.value = RedirectState.Error(
                    message = "State parameter does not match. You may be a victim of an attack."
                )

                // 4.1 Redirect the user to the home screen
                homeIntent()
                return@launch
            }

            // 5. Exchange the authorization code for user credentials using the code verifier
            val codeVerifier = preSession.codeVerifier

            authenticationService.exchangeAuthorizationCode(authorizationCode = code, codeVerifier = codeVerifier)
            _state.value = RedirectState.Authenticated

            // 6. Redirect the user to the app's main screen

        }

    }

}

@Suppress("UNCHECKED_CAST")
class RedirectViewModelFactory(
    val authenticationService: AuthenticationService,
    val initialState: RedirectState = RedirectState.Redirected
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RedirectViewModel(
            authenticationService = authenticationService,
            initialState = initialState
        ) as T
    }

}