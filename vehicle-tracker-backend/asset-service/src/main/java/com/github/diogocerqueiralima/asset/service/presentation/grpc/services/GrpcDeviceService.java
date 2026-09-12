package com.github.diogocerqueiralima.asset.service.presentation.grpc.services;

import com.github.diogocerqueiralima.asset.service.application.commands.DeviceIsOwnedByUserCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.asset.service.domain.exceptions.DeviceAlreadyExistsException;
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.DeviceUseCase;
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult;
import com.github.diogocerqueiralima.asset.service.presentation.grpc.mappers.DeviceGrpcMapper;
import com.github.diogocerqueiralima.error.common.exceptions.ForbiddenException;
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException;
import com.github.diogocerqueiralima.schema.proto.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;

/**
 * gRPC service implementation that exposes device-related operations to external clients.
 */
@GrpcService
public class GrpcDeviceService extends DeviceServiceGrpc.DeviceServiceImplBase {

    private final DeviceUseCase deviceUseCase;

    /**
     *
     * Instantiates the gRPC service with the required application use case.
     *
     * @param deviceUseCase the device use case to be used by the service.
     */
    public GrpcDeviceService(DeviceUseCase deviceUseCase) {
        this.deviceUseCase = deviceUseCase;
    }

    @Override
    public void getDeviceById(DeviceId request, StreamObserver<DeviceResponse> responseObserver) {

        try {

            // 1. Retrieve the device by id using the application use case.
            DeviceResult result = deviceUseCase.getById(
                    new GetDeviceByIdCommand(UUID.fromString(request.getId()), new UUID(0, 0), true)
            );

            // 2. Map the application result to a gRPC response and send it.
            DeviceResponse response = DeviceGrpcMapper.toResponse(result);

            // 3. Send the response and complete the RPC call.
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {

            // 4. Returns an INVALID_ARGUMENT error for any validation errors in the input data.
            responseObserver.onError(
                    new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(e.getMessage()))
            );

        } catch (NotFoundException e) {

            // 5. Returns a NOT_FOUND error if the device is not found for the provided id.
            responseObserver.onError(
                    new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage()))
            );

        } catch (DeviceAlreadyExistsException e) {

            // 6. Returns an ALREADY_EXISTS error if a device with the same serial number or IMEI already exists.
            responseObserver.onError(
                    new StatusRuntimeException(Status.ALREADY_EXISTS.withDescription(e.getMessage()))
            );

        } catch (Exception e) {

            // 7. Returns an INTERNAL error for any unexpected exceptions.
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()))
            );

        }

    }

    @Override
    public void deviceIsOwnedByUser(DeviceIsOwnedByUserRequest request, StreamObserver<DeviceIsOwnedByUserResponse> responseObserver) {

        try {

            deviceUseCase.isOwnedBy(
                    new DeviceIsOwnedByUserCommand(
                            UUID.fromString(request.getDeviceId()),
                            UUID.fromString(request.getUserId())
                    )
            );

            responseObserver.onNext(DeviceIsOwnedByUserResponse.newBuilder().build());
            responseObserver.onCompleted();

        } catch (ForbiddenException e) {
            responseObserver.onError(
                    new StatusRuntimeException(Status.PERMISSION_DENIED.withDescription(e.getMessage()))
            );
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(e.getMessage()))
            );
        } catch (Exception e) {
            responseObserver.onError(
                    new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()))
            );
        }

    }

}
