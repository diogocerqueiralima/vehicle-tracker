package com.github.diogocerqueiralima.domain.client

import com.github.diogocerqueiralima.infrastructure.http.dto.TokenDTO

interface TokenClient {

    suspend fun exchangeAuthorizationCodeForToken(
        authorizationCode: String,
        codeVerifier: String,
    ): TokenDTO

}