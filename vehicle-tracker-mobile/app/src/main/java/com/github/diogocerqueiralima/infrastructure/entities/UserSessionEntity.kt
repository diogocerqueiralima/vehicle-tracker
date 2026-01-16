package com.github.diogocerqueiralima.infrastructure.entities

import kotlinx.serialization.Serializable

@Serializable
data class AccessTokenEntity(
    val value: String,
    val createdAtEpochMillis: Long,
    val expiresInSeconds: Long,
    val renewedAtEpochMillis: Long? = null
)

@Serializable
data class RefreshTokenEntity(
    val value: String,
    val createdAtEpochMillis: Long,
    val expiresInSeconds: Long
)

@Serializable
data class IdentityTokenEntity(
    val value: String,
    val createdAtEpochMillis: Long
)

@Serializable
data class UserSessionEntity(
    val accessToken: AccessTokenEntity,
    val refreshToken: RefreshTokenEntity,
    val identityToken: IdentityTokenEntity
)