package com.github.diogocerqueiralima.domain.authentication.repositories

import com.github.diogocerqueiralima.domain.authentication.model.UserSession

/**
 * Repository interface for managing UserSessions.
 */
interface UserSessionRepository {

    /**
     *
     * Persists the given UserSession.
     * The implementation should ensure that the session is stored securely.
     *
     * Calling this method will overwrite any existing session.
     *
     * @param session The UserSession to be saved.
     */
    suspend fun save(session: UserSession)

    /**
     * @return The stored UserSession, or null if no session is found.
     */
    suspend fun get(): UserSession?

}