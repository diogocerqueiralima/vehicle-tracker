package com.github.diogocerqueiralima.infrastructure.mappers

import com.github.diogocerqueiralima.domain.model.UserPreSession
import com.github.diogocerqueiralima.infrastructure.entities.UserPreSessionEntity

/**
 * Extension function to convert a [UserPreSession] domain model to a [UserPreSessionEntity] infrastructure entity.
 */
fun UserPreSession.toEntity() = UserPreSessionEntity(
    codeVerifier = this.codeVerifier,
    state = this.state
)

/**
 * Extension function to convert a [UserPreSessionEntity] infrastructure entity to a [UserPreSession] domain model.
 */
fun UserPreSessionEntity.toDomain() = UserPreSession(
    codeVerifier = this.codeVerifier,
    state = this.state
)