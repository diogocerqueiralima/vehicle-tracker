package com.github.diogocerqueiralima.presentation.grpc.services;

import com.github.diogocerqueiralima.application.exceptions.VehicleAssignmentNotFoundException;
import com.github.diogocerqueiralima.domain.ports.inbound.VehicleAssignmentUseCase;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.presentation.grpc.mappers.VehicleAssignmentGrpcMapper;
import com.github.diogocerqueiralima.proto.DeviceId;
import com.github.diogocerqueiralima.proto.VehicleAssignmentResponse;
import com.github.diogocerqueiralima.proto.VehicleAssignmentServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

/**
 * gRPC service implementation that exposes vehicle-related operations to external clients.
 */
@GrpcService
public class GrpcVehicleAssignmentService extends VehicleAssignmentServiceGrpc.VehicleAssignmentServiceImplBase {

    private final VehicleAssignmentUseCase vehicleAssignmentUseCase;

    /**
     *
     * Instantiates the gRPC service with the required application use case.
     *
     * @param vehicleAssignmentUseCase the vehicle assignment use case to be used by the service.
     */
    public GrpcVehicleAssignmentService(VehicleAssignmentUseCase vehicleAssignmentUseCase) {
        this.vehicleAssignmentUseCase = vehicleAssignmentUseCase;
    }

    @Override
    public void getVehicleAssignmentByDeviceId(DeviceId request, StreamObserver<VehicleAssignmentResponse> responseObserver) {

        try {

            // 1. Retrieves the active vehicle assignment for the provided device id.
            VehicleAssignmentResult result = vehicleAssignmentUseCase
                    .getVehicleAssignmentByDeviceId(
                            VehicleAssignmentGrpcMapper.toGetVehicleAssignmentByDeviceIdCommand(request)
                    );

            // 2. Maps the application result to a gRPC response and sends it.
            VehicleAssignmentResponse response = VehicleAssignmentGrpcMapper.toResponse(result);

            // 3. Sends the response and completes the RPC call.
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (VehicleAssignmentNotFoundException e) {

            // 4. Returns a NOT_FOUND error if no active assignment is found for the provided device id.
            responseObserver.onError(
                    new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage()))
            );

        } catch (IllegalArgumentException e) {

            // 5. Returns an INVALID_ARGUMENT error for any validation errors in the input data.
            responseObserver.onError(
                    new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(e.getMessage()))
            );

        } catch (Exception e) {

            // 6. Returns an INTERNAL error for any unexpected exceptions.
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()))
            );

        }

    }
}
