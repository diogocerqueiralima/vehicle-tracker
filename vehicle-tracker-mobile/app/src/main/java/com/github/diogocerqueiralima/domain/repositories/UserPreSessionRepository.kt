package com.github.diogocerqueiralima.domain.repositories

import com.github.diogocerqueiralima.domain.model.UserPreSession

interface UserPreSessionRepository {

    suspend fun save(preSession: UserPreSession)

    suspend fun get(): UserPreSession?

}