package com.github.diogocerqueiralima

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import java.security.KeyStore

interface DependenciesContainer {

    val httpClient: HttpClient
    val dataStore: DataStore<Preferences>
    val keyStore: KeyStore

}

class VehicleTrackerApplication : Application(), DependenciesContainer {

    override val httpClient: HttpClient by lazy {

        HttpClient(OkHttp) {

            install(ContentNegotiation) {
                json()
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

}