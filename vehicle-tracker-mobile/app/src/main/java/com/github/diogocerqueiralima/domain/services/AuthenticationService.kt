package com.github.diogocerqueiralima.domain.services

import com.github.diogocerqueiralima.domain.client.AuthenticationClient
import com.github.diogocerqueiralima.domain.model.Token
import com.github.diogocerqueiralima.domain.model.UserSession
import com.github.diogocerqueiralima.domain.repositories.UserSessionRepository
import kotlin.time.Clock

/**
 * Service responsible for handling user authentication and session management.
 */
class AuthenticationService(
    private val client: AuthenticationClient,
    private val userSessionRepository: UserSessionRepository
) {

    /**
     * Exchanges the authorization code for a set of user credentials and saves them.
     *
     * @param authorizationCode The authorization code received from the authorization server.
     * @param codeVerifier The code verifier used in the PKCE flow.
     */
    suspend fun exchangeAuthorizationCode(authorizationCode: String, codeVerifier: String) {

        val dto = client.exchangeAuthorizationCode(authorizationCode, codeVerifier)
        val session = UserSession(
            accessToken = Token.AccessToken(
                value = dto.accessToken,
                createdAt = Clock.System.now(),
                expiresIn = dto.expiresIn,
                renewedAt = null
            ),
            refreshToken = Token.RefreshToken(
                value = dto.refreshToken,
                createdAt = Clock.System.now(),
                expiresIn = dto.refreshExpiresIn
            ),
            identityToken = Token.IdentityToken(
                value = dto.idToken,
                createdAt = Clock.System.now()
            )
        )

        userSessionRepository.save(session)
    }

}