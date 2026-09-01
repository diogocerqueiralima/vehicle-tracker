package com.github.diogocerqueiralima

import android.app.Application
import android.bluetooth.BluetoothManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.github.diogocerqueiralima.domain.authentication.repositories.UserPreSessionRepository
import com.github.diogocerqueiralima.domain.authentication.repositories.UserSessionRepository
import com.github.diogocerqueiralima.domain.authentication.services.AuthenticationService
import com.github.diogocerqueiralima.domain.authentication.services.UserSessionService
import com.github.diogocerqueiralima.infrastructure.authentication.client.AuthenticationHttpClient
import com.github.diogocerqueiralima.infrastructure.authentication.repositories.KeyRepositoryImpl
import com.github.diogocerqueiralima.infrastructure.authentication.repositories.UserPreSessionRepositoryImpl
import com.github.diogocerqueiralima.infrastructure.authentication.repositories.UserSessionRepositoryImpl
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
    val bluetoothManager: BluetoothManager
    val userSessionService: UserSessionService
    val authenticationService: AuthenticationService

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

    override val bluetoothManager: BluetoothManager by lazy {
        getSystemService(BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
    }

    private val userSessionRepository: UserSessionRepository by lazy {
        UserSessionRepositoryImpl(dataStore, KeyRepositoryImpl(keyStore))
    }

    private val userPreSessionRepository: UserPreSessionRepository by lazy {
        UserPreSessionRepositoryImpl(dataStore)
    }

    override val userSessionService: UserSessionService by lazy {
        UserSessionService(userSessionRepository)
    }

    override val authenticationService: AuthenticationService by lazy {
        AuthenticationService(AuthenticationHttpClient(httpClient), userSessionRepository, userPreSessionRepository)
    }

}