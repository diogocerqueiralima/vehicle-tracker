package com.github.diogocerqueiralima.domain.model

import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

sealed interface Token {

    val value: String
    val createdAt: Instant

    sealed interface SessionToken : Token {

        val expiresIn: Long

        val expiresAt: Instant
            get() = createdAt + expiresIn.seconds

        fun isExpired(at: Instant = Clock.System.now()): Boolean {
            return at >= expiresAt
        }

    }

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

    class RefreshToken(
        override val value: String,
        override val createdAt: Instant,
        override val expiresIn: Long,
    ) : SessionToken

    class IdentityToken(
        override val value: String,
        override val createdAt: Instant,
    ) : Token

}

data class UserSession(
    val accessToken: Token.AccessToken,
    val refreshToken: Token.RefreshToken,
    val identityToken: Token.IdentityToken
) {

    fun shouldRefreshSession(at: Instant = Clock.System.now()): Boolean =
        accessToken.isExpired(at)

    fun shouldReauthenticate(at: Instant = Clock.System.now()): Boolean =
        refreshToken.isExpired(at)

}