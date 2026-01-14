package com.github.diogocerqueiralima.domain.client

import com.github.diogocerqueiralima.infrastructure.http.dto.ExchangeAuthorizationCodeResponseDTO

interface AuthenticationClient {

    suspend fun exchangeAuthorizationCode(
        authorizationCode: String,
        codeVerifier: String,
    ): ExchangeAuthorizationCodeResponseDTO

}