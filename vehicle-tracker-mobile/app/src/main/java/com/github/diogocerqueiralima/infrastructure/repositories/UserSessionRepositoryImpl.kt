package com.github.diogocerqueiralima.infrastructure.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.github.diogocerqueiralima.domain.model.UserSession
import com.github.diogocerqueiralima.domain.repositories.UserSessionRepository

class UserSessionRepositoryImpl(private val dataStore: DataStore<Preferences>) : UserSessionRepository {

    override suspend fun save(session: UserSession) {
        TODO("Not yet implemented")
    }

}