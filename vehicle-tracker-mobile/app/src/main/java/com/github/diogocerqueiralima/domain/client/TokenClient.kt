package com.github.diogocerqueiralima.domain.client

interface TokenClient {

    suspend fun exchangeAuthorizationCodeForToken(
        authorizationCode: String,
        codeVerifier: String,
        redirectUri: String
    ): String

}