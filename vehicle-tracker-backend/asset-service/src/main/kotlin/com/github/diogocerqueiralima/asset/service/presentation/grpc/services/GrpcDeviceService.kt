package com.github.diogocerqueiralima.asset.service.presentation.grpc.services

import com.github.diogocerqueiralima.error.common.exceptions.ConflictException
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.DeviceUseCase
import com.github.diogocerqueiralima.asset.service.presentation.grpc.mappers.DeviceGrpcMapper
import com.github.diogocerqueiralima.schema.proto.DeviceId
import com.github.diogocerqueiralima.schema.proto.DeviceResponse
import com.github.diogocerqueiralima.schema.proto.DeviceServiceGrpc
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import org.springframework.grpc.server.service.GrpcService
import java.util.UUID

@GrpcService
class GrpcDeviceService(private val deviceUseCase: DeviceUseCase) : DeviceServiceGrpc.DeviceServiceImplBase() {

    override fun getDeviceById(request: DeviceId, responseObserver: StreamObserver<DeviceResponse>) {

        try {

            // 1. Retrieve the device by id using the application use case.
            val result = deviceUseCase.getById(
                GetDeviceByIdCommand(UUID.fromString(request.id), UUID(0, 0), true)
            )

            // 2. Map the application result to a gRPC response and send it.
            val response = DeviceGrpcMapper.toResponse(result)

            // 3. Send the response and complete the RPC call.
            responseObserver.onNext(response)
            responseObserver.onCompleted()

        } catch (e: IllegalArgumentException) {

            // 4. Returns an INVALID_ARGUMENT error for any validation errors in the input data.
            responseObserver.onError(StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(e.message)))

        } catch (e: NotFoundException) {

            // 5. Returns a NOT_FOUND error if the device is not found for the provided id.
            responseObserver.onError(StatusRuntimeException(Status.NOT_FOUND.withDescription(e.message)))

        } catch (e: ConflictException) {

            // 6. Returns an ALREADY_EXISTS error if a device with the same serial number or IMEI already exists.
            responseObserver.onError(StatusRuntimeException(Status.ALREADY_EXISTS.withDescription(e.message)))

        } catch (e: Exception) {

            // 7. Returns an INTERNAL error for any unexpected exceptions.
            responseObserver.onError(StatusRuntimeException(Status.INTERNAL.withDescription(e.message)))

        }

    }

}
