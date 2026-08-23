package com.github.diogocerqueiralima

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.github.diogocerqueiralima.domain.repositories.UserPreSessionRepository
import com.github.diogocerqueiralima.domain.repositories.UserSessionRepository
import com.github.diogocerqueiralima.infrastructure.repositories.KeyRepositoryImpl
import com.github.diogocerqueiralima.infrastructure.repositories.UserPreSessionRepositoryImpl
import com.github.diogocerqueiralima.infrastructure.repositories.UserSessionRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import java.security.KeyStore

interface DependenciesContainer {

    val httpClient: HttpClient
    val dataStore: DataStore<Preferences>
    val keyStore: KeyStore
    val userSessionRepository: UserSessionRepository
    val userPreSessionRepository: UserPreSessionRepository

}

class VehicleTrackerApplication : Application(), DependenciesContainer {

    override val httpClient: HttpClient by lazy {

        HttpClient(OkHttp) {

            install(ContentNegotiation) {
                json()
            }

            install(Auth) {

                bearer {

                    loadTokens {

                        val userSession = userSessionRepository.get()
                        userSession?.let {
                            BearerTokens(
                                accessToken = it.accessToken.value,
                                refreshToken = it.refreshToken.value
                            )
                        }

                    }

                }

            }

            engine {

                config {
                    followRedirects(false)
                }

            }

        }

    }

    override val dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

    override val keyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    }

    override val userSessionRepository: UserSessionRepository by lazy {
        UserSessionRepositoryImpl(dataStore, KeyRepositoryImpl(keyStore))
    }

    override val userPreSessionRepository: UserPreSessionRepository by lazy {
        UserPreSessionRepositoryImpl(dataStore)
    }

}