package com.github.diogocerqueiralima

import android.app.Application
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

interface DependenciesContainer {

    val httpClient: HttpClient

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

}