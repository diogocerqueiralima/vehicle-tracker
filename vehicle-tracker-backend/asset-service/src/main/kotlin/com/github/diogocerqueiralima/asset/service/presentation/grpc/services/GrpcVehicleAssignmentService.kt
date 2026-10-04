package com.github.diogocerqueiralima.asset.service.presentation.grpc.services

import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.VehicleAssignmentUseCase
import com.github.diogocerqueiralima.asset.service.presentation.grpc.mappers.VehicleAssignmentGrpcMapper
import com.github.diogocerqueiralima.schema.proto.DeviceId
import com.github.diogocerqueiralima.schema.proto.VehicleAssignmentResponse
import com.github.diogocerqueiralima.schema.proto.VehicleAssignmentServiceGrpc
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class GrpcVehicleAssignmentService(
    private val vehicleAssignmentUseCase: VehicleAssignmentUseCase
) : VehicleAssignmentServiceGrpc.VehicleAssignmentServiceImplBase() {

    override fun getVehicleAssignmentByDeviceId(
        request: DeviceId, responseObserver: StreamObserver<VehicleAssignmentResponse>
    ) {

        try {

            // 1. Retrieves the active vehicle assignment for the provided device id.
            val result = vehicleAssignmentUseCase.getVehicleAssignmentByDeviceId(
                VehicleAssignmentGrpcMapper.toGetVehicleAssignmentByDeviceIdCommand(request)
            )

            // 2. Maps the application result to a gRPC response and sends it.
            val response = VehicleAssignmentGrpcMapper.toResponse(result)

            // 3. Sends the response and completes the RPC call.
            responseObserver.onNext(response)
            responseObserver.onCompleted()

        } catch (e: NotFoundException) {

            // 4. Returns a NOT_FOUND error if no active assignment is found for the provided device id.
            responseObserver.onError(StatusRuntimeException(Status.NOT_FOUND.withDescription(e.message)))

        } catch (e: IllegalArgumentException) {

            // 5. Returns an INVALID_ARGUMENT error for any validation errors in the input data.
            responseObserver.onError(StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(e.message)))

        } catch (e: Exception) {

            // 6. Returns an INTERNAL error for any unexpected exceptions.
            responseObserver.onError(StatusRuntimeException(Status.INTERNAL.withDescription(e.message)))

        }

    }

}
