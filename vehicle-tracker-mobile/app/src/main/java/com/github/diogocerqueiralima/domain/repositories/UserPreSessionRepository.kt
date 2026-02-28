package com.github.diogocerqueiralima.domain.repositories

import com.github.diogocerqueiralima.domain.model.UserPreSession

/**
 * Repository interface for managing UserPreSessions.
 */
interface UserPreSessionRepository {

    /**
     * Persists the given UserPreSession.
     *
     * @param preSession The UserPreSession to be saved.
     */
    suspend fun save(preSession: UserPreSession)

    /**
     * Retrieves the currently stored UserPreSession, if any.
     *
     * @return The stored UserPreSession, or null if no session is stored.
     */
    suspend fun get(): UserPreSession?

}