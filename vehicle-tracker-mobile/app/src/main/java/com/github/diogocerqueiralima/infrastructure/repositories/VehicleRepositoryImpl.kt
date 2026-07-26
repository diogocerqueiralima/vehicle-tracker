package com.github.diogocerqueiralima.infrastructure.repositories

import com.github.diogocerqueiralima.domain.model.Vehicle
import com.github.diogocerqueiralima.domain.repositories.VehicleRepository
import com.github.diogocerqueiralima.infrastructure.entities.VehicleEntity
import com.github.diogocerqueiralima.infrastructure.mappers.toDomain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.append
import java.util.UUID

/**
 * Implementation of [VehicleRepository] using [HttpClient] to retrieve vehicle data from a remote source.
 */
class VehicleRepositoryImpl(private val httpClient: HttpClient) : VehicleRepository {

    override suspend fun findById(id: UUID): Vehicle? {

        val response = httpClient.get("") {

            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json)
            }

        }

        val entity = response.body<VehicleEntity?>()
        return entity?.toDomain()
    }

}