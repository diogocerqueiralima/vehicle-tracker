package com.github.diogocerqueiralima.domain.repositories

import com.github.diogocerqueiralima.domain.model.UserSession

interface UserSessionRepository {

    suspend fun save(session: UserSession)

}