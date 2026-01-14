package com.github.diogocerqueiralima.domain.services

import com.github.diogocerqueiralima.domain.client.AuthenticationClient

class AuthenticationService(private val client: AuthenticationClient) {

    /**
     * Exchanges the authorization code for a set of user credentials and saves them.
     *
     * @param authorizationCode The authorization code received from the authorization server.
     * @param codeVerifier The code verifier used in the PKCE flow.
     */
    suspend fun exchangeAuthorizationCode(authorizationCode: String, codeVerifier: String) {

        val dto = client.exchangeAuthorizationCode(authorizationCode, codeVerifier)

    }

}