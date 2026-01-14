package com.github.diogocerqueiralima.domain.services

import com.github.diogocerqueiralima.domain.client.TokenClient

class TokenService(private val client: TokenClient) {

    /**
     * Exchanges the authorization code for an access token and saves it.
     *
     * @param authorizationCode The authorization code received from the authorization server.
     * @param codeVerifier The code verifier used in the PKCE flow.
     */
    suspend fun getTokenAndSave(authorizationCode: String, codeVerifier: String) {

        val dto = client.exchangeAuthorizationCodeForToken(authorizationCode, codeVerifier)

    }

}