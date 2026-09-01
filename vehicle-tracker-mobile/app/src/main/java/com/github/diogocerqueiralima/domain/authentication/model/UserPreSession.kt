package com.github.diogocerqueiralima.domain.authentication.model

/**
 * Represents a user's pre-session information, including the code verifier and state used in the authentication process.
 *
 * @property codeVerifier The code verifier used in the PKCE flow.
 * @property state The state parameter used to maintain state between the request and callback.
 */
data class UserPreSession(val codeVerifier: String, val state: String)
