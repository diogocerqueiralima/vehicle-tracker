package com.github.diogocerqueiralima.identity.service.infrastructure.mappers

import com.github.diogocerqueiralima.identity.service.domain.model.device.Device
import com.github.diogocerqueiralima.schema.proto.DeviceResponse
import java.util.UUID

/**
 * This object is responsible for mapping between Device domain objects and their corresponding gRPC responses.
 */
object DeviceMapper {

    /**
     * Converts a [DeviceResponse] object to a [Device] domain object.
     *
     * @param response the [DeviceResponse] object to be converted
     * @return a [Device] domain object corresponding to the provided response
     */
    fun toDomain(response: DeviceResponse): Device =
        Device(
            UUID.fromString(response.id),
            UUID.fromString(response.ownerId)
        )

}
