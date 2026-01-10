package com.github.diogocerqueiralima.infrastructure.http

import com.github.diogocerqueiralima.domain.client.TokenClient
import com.github.diogocerqueiralima.infrastructure.http.dto.TokenDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.headers

class TokenHttpClient(private val client: HttpClient) : TokenClient {

    override suspend fun exchangeAuthorizationCodeForToken(
        authorizationCode: String,
        codeVerifier: String,
        redirectUri: String
    ): String {

        val response = client.get(urlString = "") {
            headers {
                append("accept", "application/json")
            }
        }

        val dto = response.body<TokenDTO>()

        return dto.accessToken
    }

}