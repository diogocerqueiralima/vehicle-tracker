package com.github.diogocerqueiralima.infrastructure.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.diogocerqueiralima.domain.model.UserPreSession
import com.github.diogocerqueiralima.domain.repositories.UserPreSessionRepository
import com.github.diogocerqueiralima.infrastructure.entities.UserPreSessionEntity
import com.github.diogocerqueiralima.infrastructure.mappers.toDomain
import com.github.diogocerqueiralima.infrastructure.mappers.toEntity
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

const val USER_PRE_SESSION_DATA = "USER_PRE_SESSION_DATA"

class UserPreSessionRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : UserPreSessionRepository {

    override suspend fun save(preSession: UserPreSession) {

        val entity = preSession.toEntity()
        val json = Json.encodeToString(entity)

        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("USER_PRE_SESSION_DATA")] = json
        }

    }

    override suspend fun get(): UserPreSession? {

        val preferences = dataStore.data.first()
        val json = preferences[stringPreferencesKey("USER_PRE_SESSION_DATA")] ?: return null
        val entity = Json.decodeFromString<UserPreSessionEntity>(json)

        return entity.toDomain()
    }

}