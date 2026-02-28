package com.github.diogocerqueiralima.infrastructure.entities

import kotlinx.serialization.Serializable

@Serializable
data class UserPreSessionEntity(val codeVerifier: String, val state: String)