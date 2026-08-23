package com.github.diogocerqueiralima.domain.services

import com.github.diogocerqueiralima.domain.model.UserSession
import com.github.diogocerqueiralima.domain.repositories.UserSessionRepository

/**
 * Service responsible for exposing the currently authenticated user's session, along with their identity claims.
 */
class UserSessionService(
    private val userSessionRepository: UserSessionRepository
) {

    /**
     * Retrieves the current user session with its identity decoded from the identity token.
     *
     * @throws IllegalStateException if there is no active user session.
     */
    suspend fun get(): UserSession? {
        return userSessionRepository.get()
    }

}
