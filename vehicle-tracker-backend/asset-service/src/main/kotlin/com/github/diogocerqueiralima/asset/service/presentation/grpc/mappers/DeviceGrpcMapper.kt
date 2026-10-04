package com.github.diogocerqueiralima.asset.service.presentation.grpc.mappers

import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult
import com.github.diogocerqueiralima.schema.proto.DeviceResponse
import com.github.diogocerqueiralima.schema.proto.deviceResponse

object DeviceGrpcMapper {

    fun toResponse(result: DeviceResult): DeviceResponse =
        deviceResponse {
            id = result.id.toString()
            ownerId = checkNotNull(result.ownerId) { "Device ${result.id} has no owner" }.toString()
            createdAt = result.createdAt.toProtoTimestamp()
            updatedAt = result.updatedAt.toProtoTimestamp()
            serialNumber = result.serialNumber
            imei = result.imei
            manufacturer = result.manufacturer
            model = result.model
        }

}
