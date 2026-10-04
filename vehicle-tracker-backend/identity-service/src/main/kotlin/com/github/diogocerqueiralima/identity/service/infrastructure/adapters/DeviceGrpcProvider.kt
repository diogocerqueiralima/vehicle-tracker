package com.github.diogocerqueiralima.identity.service.infrastructure.adapters

import com.github.diogocerqueiralima.identity.service.domain.model.device.Device
import com.github.diogocerqueiralima.identity.service.domain.ports.outbound.DeviceProvider
import com.github.diogocerqueiralima.identity.service.infrastructure.mappers.DeviceMapper
import com.github.diogocerqueiralima.schema.proto.DeviceId
import com.github.diogocerqueiralima.schema.proto.DeviceServiceGrpc
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Implementation of the [DeviceProvider] interface that uses gRPC to communicate with a remote device service.
 * This class provides methods to retrieve device information from the remote service.
 * It uses a gRPC blocking stub to make synchronous calls to the device service.
 *
 * @param blockingStub the gRPC blocking stub used to communicate with the remote device service
 */
@Component
class DeviceGrpcProvider(
    private val blockingStub: DeviceServiceGrpc.DeviceServiceBlockingStub
) : DeviceProvider {

    override fun findById(id: UUID): Device? {

        try {

            val response = blockingStub.getDeviceById(
                DeviceId.newBuilder()
                    .setId(id.toString())
                    .build()
            )

            return DeviceMapper.toDomain(response)

        } catch (e: StatusRuntimeException) {

            if (e.status.code == Status.Code.NOT_FOUND) {
                return null
            }

            throw e
        }

    }

}
