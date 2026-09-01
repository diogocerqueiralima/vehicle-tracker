package com.github.diogocerqueiralima.infrastructure.authentication.client

import android.util.Log
import com.github.diogocerqueiralima.BuildConfig
import com.github.diogocerqueiralima.domain.authentication.client.AuthenticationClient
import com.github.diogocerqueiralima.domain.authentication.model.UserSession
import com.github.diogocerqueiralima.infrastructure.authentication.dto.ExchangeAuthorizationCodeResponseDTO
import com.github.diogocerqueiralima.infrastructure.authentication.mappers.toSession
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.append
import io.ktor.http.headers

const val TAG = "TOKEN_HTTP_CLIENT"

/**
 * Implementation of [AuthenticationClient] using Ktor HTTP client.
 */
class AuthenticationHttpClient(private val client: HttpClient) : AuthenticationClient {

    override suspend fun exchangeAuthorizationCode(
        authorizationCode: String,
        codeVerifier: String,
    ): UserSession {

        val response = client.post(urlString = BuildConfig.TOKEN_URI) {

            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json)
                append(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            }

            setBody(
                FormDataContent(
                    Parameters.build {
                        append("grant_type", "authorization_code")
                        append("code", authorizationCode)
                        append("code_verifier", codeVerifier)
                        append("redirect_uri", BuildConfig.REDIRECT_URI)
                        append("client_id", BuildConfig.CLIENT_ID)
                    }
                )
            )

        }

        val dto = response.body<ExchangeAuthorizationCodeResponseDTO>()

        Log.d(TAG, "Received token DTO: $dto")
        return dto.toSession()
    }

}