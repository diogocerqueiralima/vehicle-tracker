package com.github.diogocerqueiralima.domain.client

import com.github.diogocerqueiralima.domain.model.UserSession

/**
 * Client responsible for handling authentication-related operations.
 */
interface AuthenticationClient {

    /**
     * This function exchanges an authorization code and code verifier for tokens.
     *
     * @param authorizationCode The authorization code received from the authorization server.
     * @param codeVerifier The code verifier used in the PKCE flow.
     * @return An instance of [UserSession] containing the access token, refresh token, and other session information.
     */
    suspend fun exchangeAuthorizationCode(
        authorizationCode: String,
        codeVerifier: String,
    ): UserSession

}