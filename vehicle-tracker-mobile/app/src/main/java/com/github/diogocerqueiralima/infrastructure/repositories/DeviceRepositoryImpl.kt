package com.github.diogocerqueiralima.infrastructure.repositories

import com.github.diogocerqueiralima.BuildConfig
import com.github.diogocerqueiralima.domain.model.Device
import com.github.diogocerqueiralima.domain.repositories.DeviceRepository
import com.github.diogocerqueiralima.infrastructure.entities.DeviceEntity
import com.github.diogocerqueiralima.infrastructure.dto.ApiResponseDTO
import com.github.diogocerqueiralima.infrastructure.dto.CreateDeviceDTO
import com.github.diogocerqueiralima.infrastructure.dto.PageDTO
import com.github.diogocerqueiralima.infrastructure.mappers.toDomain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.append
import io.ktor.http.contentType
import java.util.UUID

/**
 * Implementation of [DeviceRepository] using [HttpClient] to retrieve device data from a remote source.
 */
class DeviceRepositoryImpl(private val httpClient: HttpClient) : DeviceRepository {

    private companion object {

        const val DEVICES_BASE_URI = "${BuildConfig.ASSET_SERVICE_URI}/devices"
        const val DEVICES_ID_URI = "$DEVICES_BASE_URI/{id}"

    }

    override suspend fun findById(id: UUID): Device? {

        val response = httpClient.get(DEVICES_ID_URI.replace("{id}", id.toString())) {

            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json)
            }

        }

        val entity = response.body<DeviceEntity?>()
        return entity?.toDomain()
    }

    override suspend fun findAll(): List<Device> {

        val response = httpClient.get(DEVICES_BASE_URI) {

            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json)
            }

        }

        val dto = response.body<ApiResponseDTO<PageDTO<DeviceEntity>>>()
        return dto.data?.data?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun create(serialNumber: String, model: String, manufacturer: String, imei: String, ownerId: UUID): Device {

        val response = httpClient.post(DEVICES_BASE_URI) {

            contentType(ContentType.Application.Json)
            setBody(
                CreateDeviceDTO(
                    serialNumber = serialNumber,
                    model = model,
                    manufacturer = manufacturer,
                    imei = imei,
                    ownerId = ownerId.toString()
                )
            )

        }

        val dto = response.body<ApiResponseDTO<DeviceEntity>>()
        return dto.data!!.toDomain()
    }

}
