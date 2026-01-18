package com.github.diogocerqueiralima.domain.model

import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Represents a token used for authentication and authorization.
 */
sealed interface Token {

    val value: String
    val createdAt: Instant

    /**
     * Represents a session token that has an expiration time.
     */
    sealed interface SessionToken : Token {

        val expiresIn: Long

        val expiresAt: Instant
            get() = createdAt + expiresIn.seconds

        fun isExpired(at: Instant = Clock.System.now()): Boolean {
            return at >= expiresAt
        }

    }

    /**
     * Represents an access token used to access protected resources.
     */
    class AccessToken(
        override val value: String,
        override val createdAt: Instant,
        override val expiresIn: Long,
        val renewedAt: Instant?
    ) : SessionToken {

        override val expiresAt: Instant
            get() {
                val base = renewedAt ?: createdAt
                return base + expiresIn.seconds
            }

    }

    /**
     * Represents a refresh token used to obtain new access tokens.
     */
    class RefreshToken(
        override val value: String,
        override val createdAt: Instant,
        override val expiresIn: Long,
    ) : SessionToken

    /**
     * Represents an identity token containing user identity information.
     */
    class IdentityToken(
        override val value: String,
        override val createdAt: Instant,
    ) : Token

}

/**
 * Represents a user session containing access, refresh, and identity tokens.
 */
data class UserSession(
    val accessToken: Token.AccessToken,
    val refreshToken: Token.RefreshToken,
    val identityToken: Token.IdentityToken
) {

    /**
     * Determines if the access token should be refreshed.
     *
     * @param at The current time to check against. Defaults to the current system time.
     * @return True if the access token is expired, false otherwise.
     */
    fun shouldRefreshSession(at: Instant = Clock.System.now()): Boolean =
        accessToken.isExpired(at)

    /**
     * Determines if the user should re-authenticate.
     * User should re-authenticate if the refresh token is expired.
     *
     * @param at The current time to check against. Defaults to the current system time.
     * @return True if the refresh token is expired, false otherwise.
     */
    fun shouldReauthenticate(at: Instant = Clock.System.now()): Boolean =
        refreshToken.isExpired(at)

}