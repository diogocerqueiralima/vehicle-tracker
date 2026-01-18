package com.github.diogocerqueiralima.domain.client

import com.github.diogocerqueiralima.infrastructure.http.dto.ExchangeAuthorizationCodeResponseDTO

/**
 * Client responsible for handling authentication-related operations.
 */
interface AuthenticationClient {

    /**
     * This function exchanges an authorization code and code verifier for tokens.
     *
     * @param authorizationCode The authorization code received from the authorization server.
     * @param codeVerifier The code verifier used in the PKCE flow.
     * @return An [ExchangeAuthorizationCodeResponseDTO] containing the tokens.
     */
    suspend fun exchangeAuthorizationCode(
        authorizationCode: String,
        codeVerifier: String,
    ): ExchangeAuthorizationCodeResponseDTO

}