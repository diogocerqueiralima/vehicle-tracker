package com.github.diogocerqueiralima.infrastructure.authentication.mappers

import com.github.diogocerqueiralima.domain.authentication.model.Token
import com.github.diogocerqueiralima.domain.authentication.model.UserSession
import com.github.diogocerqueiralima.infrastructure.authentication.entities.AccessTokenEntity
import com.github.diogocerqueiralima.infrastructure.authentication.entities.IdentityTokenEntity
import com.github.diogocerqueiralima.infrastructure.authentication.entities.RefreshTokenEntity
import com.github.diogocerqueiralima.infrastructure.authentication.entities.UserSessionEntity
import com.github.diogocerqueiralima.infrastructure.authentication.dto.ExchangeAuthorizationCodeResponseDTO
import com.github.diogocerqueiralima.utils.decodeIdentity
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Maps a [UserSession] domain model to a [UserSessionEntity] infrastructure entity.
 * This function creates a new [UserSessionEntity] using the tokens and their associated metadata from the [UserSession].
 * The creation time for each token is converted to epoch milliseconds for storage in the entity.
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
 * Maps a [UserSessionEntity] infrastructure entity to a [UserSession] domain model.
 * This function creates a new [UserSession] using the tokens and their associated metadata from the entity.
 * The creation time for each token is converted from epoch milliseconds to [Instant] for use in the domain model.
 */
fun UserSessionEntity.toSession(): UserSession = UserSession(
    accessToken = Token.AccessToken(
        value = this.accessToken.value,
        createdAt = Instant.fromEpochMilliseconds(this.accessToken.createdAtEpochMillis),
        expiresIn = this.accessToken.expiresInSeconds,
        renewedAt = this.accessToken.renewedAtEpochMillis?.let { Instant.fromEpochMilliseconds(it) }
    ),
    refreshToken = Token.RefreshToken(
        value = this.refreshToken.value,
        createdAt = Instant.fromEpochMilliseconds(this.refreshToken.createdAtEpochMillis),
        expiresIn = this.refreshToken.expiresInSeconds
    ),
    identityToken = Token.IdentityToken(
        value = this.identityToken.value,
        createdAt = Instant.fromEpochMilliseconds(this.identityToken.createdAtEpochMillis)
    ),
    identity = decodeIdentity(this.identityToken.value)
)

/**
 * Maps an [ExchangeAuthorizationCodeResponseDTO] to a [UserSession] domain model.
 * This function creates a new [UserSession] using the tokens and their associated metadata from the response DTO.
 * The creation time for each token is set to the current system time.
 */
fun ExchangeAuthorizationCodeResponseDTO.toSession(): UserSession = UserSession(
    accessToken = Token.AccessToken(
        value = this.accessToken,
        createdAt = Clock.System.now(),
        expiresIn = this.expiresIn,
        renewedAt = null
    ),
    refreshToken = Token.RefreshToken(
        value = this.refreshToken,
        createdAt = Clock.System.now(),
        expiresIn = this.refreshExpiresIn
    ),
    identityToken = Token.IdentityToken(
        value = this.idToken,
        createdAt = Clock.System.now()
    ),
    identity = decodeIdentity(this.idToken)
)