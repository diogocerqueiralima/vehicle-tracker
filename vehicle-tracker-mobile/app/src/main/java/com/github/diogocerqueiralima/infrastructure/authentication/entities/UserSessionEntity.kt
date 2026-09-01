package com.github.diogocerqueiralima.infrastructure.authentication.entities

import kotlinx.serialization.Serializable

/**
 * Data class representing an access token entity with its value, creation time, expiration time, and optional renewal time.
 */
@Serializable
data class AccessTokenEntity(
    val value: String,
    val createdAtEpochMillis: Long,
    val expiresInSeconds: Long,
    val renewedAtEpochMillis: Long? = null
)

/**
 * Data class representing a refresh token entity with its value, creation time, and expiration time.
 */
@Serializable
data class RefreshTokenEntity(
    val value: String,
    val createdAtEpochMillis: Long,
    val expiresInSeconds: Long
)

/**
 * Data class representing an identity token entity with its value and creation time.
 */
@Serializable
data class IdentityTokenEntity(
    val value: String,
    val createdAtEpochMillis: Long
)

/**
 * Data class representing a user session entity containing access token, refresh token, and identity token.
 */
@Serializable
data class UserSessionEntity(
    val accessToken: AccessTokenEntity,
    val refreshToken: RefreshTokenEntity,
    val identityToken: IdentityTokenEntity
)