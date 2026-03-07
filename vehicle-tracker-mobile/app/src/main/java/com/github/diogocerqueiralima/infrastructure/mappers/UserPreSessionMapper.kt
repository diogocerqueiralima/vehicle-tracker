package com.github.diogocerqueiralima.infrastructure.mappers

import com.github.diogocerqueiralima.domain.model.UserPreSession
import com.github.diogocerqueiralima.infrastructure.entities.UserPreSessionEntity

fun UserPreSession.toEntity() = UserPreSessionEntity(
    codeVerifier = this.codeVerifier,
    state = this.state
)

fun UserPreSessionEntity.toDomain() = UserPreSession(
    codeVerifier = this.codeVerifier,
    state = this.state
)