package com.github.diogocerqueiralima.presentation.grpc.services;

import com.github.diogocerqueiralima.application.commands.LookupVehicleByDeviceIdCommand;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.ports.inbound.VehicleUseCase;
import com.github.diogocerqueiralima.proto.DeviceId;
import com.github.diogocerqueiralima.proto.VehicleResponse;
import com.github.diogocerqueiralima.proto.VehicleServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;

/**
 * gRPC service implementation for vehicle-related operations.
 */
@GrpcService
public class VehicleGrpcService extends VehicleServiceGrpc.VehicleServiceImplBase {

    private final VehicleUseCase vehicleUseCase;

    public VehicleGrpcService(VehicleUseCase vehicleUseCase) {
        this.vehicleUseCase = vehicleUseCase;
    }

    /**
     *
     * Gets a vehicle by device id.
     *
     * @param request the device ID request
     * @param responseObserver the response observer to send the vehicle response
     */
    @Override
    public void getByDeviceId(DeviceId request, StreamObserver<VehicleResponse> responseObserver) {

        UUID id = UUID.fromString(request.getId());
        LookupVehicleByDeviceIdCommand command = new LookupVehicleByDeviceIdCommand(id);

        try {

            VehicleResult result = vehicleUseCase.getByDeviceId(command);
            VehicleResponse response = VehicleResponse.newBuilder()
                    .setId(result.id().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (VehicleNotFoundException e) {

            StatusRuntimeException exception = new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription(e.getMessage())
            );

            responseObserver.onError(exception);
        }

    }

}
