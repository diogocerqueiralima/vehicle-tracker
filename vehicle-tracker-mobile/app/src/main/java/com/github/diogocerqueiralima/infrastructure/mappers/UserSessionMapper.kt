package com.github.diogocerqueiralima.infrastructure.mappers

import com.github.diogocerqueiralima.domain.model.Token
import com.github.diogocerqueiralima.domain.model.UserSession
import com.github.diogocerqueiralima.infrastructure.entities.AccessTokenEntity
import com.github.diogocerqueiralima.infrastructure.entities.IdentityTokenEntity
import com.github.diogocerqueiralima.infrastructure.entities.RefreshTokenEntity
import com.github.diogocerqueiralima.infrastructure.entities.UserSessionEntity
import kotlin.time.Instant

/**
 * Maps a UserSession domain model to a UserSessionEntity for persistence.
 */
fun UserSession.toEntity(): UserSessionEntity =
    UserSessionEntity(
        accessToken = AccessTokenEntity(
            value = accessToken.value,
            createdAtEpochMillis = accessToken.createdAt.toEpochMilliseconds(),
            expiresInSeconds = accessToken.expiresIn,
            renewedAtEpochMillis = accessToken.renewedAt?.toEpochMilliseconds()
        ),
        refreshToken = RefreshTokenEntity(
            value = refreshToken.value,
            createdAtEpochMillis = refreshToken.createdAt.toEpochMilliseconds(),
            expiresInSeconds = refreshToken.expiresIn
        ),
        identityToken = IdentityTokenEntity(
            value = identityToken.value,
            createdAtEpochMillis = identityToken.createdAt.toEpochMilliseconds()
        )
    )

/**
 * Maps a UserSessionEntity to a UserSession domain model.
 */
fun UserSessionEntity.toDomain(): UserSession =
    UserSession(
        accessToken = Token.AccessToken(
            value = accessToken.value,
            createdAt = Instant.fromEpochMilliseconds(accessToken.createdAtEpochMillis),
            expiresIn = accessToken.expiresInSeconds,
            renewedAt = accessToken.renewedAtEpochMillis
                ?.let { Instant.fromEpochMilliseconds(it) }
        ),
        refreshToken = Token.RefreshToken(
            value = refreshToken.value,
            createdAt = Instant.fromEpochMilliseconds(refreshToken.createdAtEpochMillis),
            expiresIn = refreshToken.expiresInSeconds
        ),
        identityToken = Token.IdentityToken(
            value = identityToken.value,
            createdAt = Instant.fromEpochMilliseconds(identityToken.createdAtEpochMillis)
        )
    )