package com.github.diogocerqueiralima

import android.app.Application
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

interface DependenciesContainer {

    val httpClient: HttpClient

}

class VehicleTrackerApplication : Application(), DependenciesContainer {

    override val httpClient: HttpClient by lazy {

        HttpClient(OkHttp) {

            engine {

                config {
                    followRedirects(false)
                }

            }

        }

    }

}