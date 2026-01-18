package com.github.diogocerqueiralima.infrastructure.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import com.github.diogocerqueiralima.domain.model.UserSession
import com.github.diogocerqueiralima.domain.repositories.KeyRepository
import com.github.diogocerqueiralima.domain.repositories.USER_SESSION_KEY
import com.github.diogocerqueiralima.domain.repositories.UserSessionRepository
import com.github.diogocerqueiralima.infrastructure.mappers.toEntity
import com.github.diogocerqueiralima.utils.encrypt

const val TAG = "USER_SESSION_REPOSITORY"
const val USER_SESSION_DATA = "USER_SESSION_DATA"

/**
 * Implementation of [UserSessionRepository] using [DataStore] for persistence.
 */
class UserSessionRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val keyRepository: KeyRepository
) : UserSessionRepository {

    override suspend fun save(session: UserSession) {

        Log.d(TAG, "Saving user session...")

        val entity = session.toEntity()
        val secretKey = keyRepository.getOrCreate(USER_SESSION_KEY)
        val encryptedUserSession = encrypt(entity, secretKey)

        dataStore.edit { preferences ->
            preferences[byteArrayPreferencesKey(USER_SESSION_DATA)] = encryptedUserSession
        }

    }

}