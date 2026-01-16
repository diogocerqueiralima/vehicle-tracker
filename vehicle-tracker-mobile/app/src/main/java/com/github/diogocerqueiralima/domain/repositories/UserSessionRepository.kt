package com.github.diogocerqueiralima.domain.repositories

import com.github.diogocerqueiralima.domain.model.UserSession

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

}