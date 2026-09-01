package com.github.diogocerqueiralima.infrastructure.authentication.entities

import kotlinx.serialization.Serializable

/**
 * Data class representing a user pre-session entity with a code verifier and state.
 */
@Serializable
data class UserPreSessionEntity(val codeVerifier: String, val state: String)