package com.github.diogocerqueiralima.presentation.grpc;

import com.github.diogocerqueiralima.application.commands.LookupVehicleByIdCommand;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.ports.inbound.VehicleService;
import com.github.diogocerqueiralima.presentation.context.ExecutionContext;
import com.github.diogocerqueiralima.presentation.context.InternalExecutionContext;
import com.github.diogocerqueiralima.proto.Vehicle;
import com.github.diogocerqueiralima.proto.VehicleId;
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

    private final VehicleService vehicleService;

    public VehicleGrpcService(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     *
     * Gets a vehicle by its ID.
     *
     * @param request the vehicle ID request
     * @param responseObserver the response observer to send the vehicle response
     */
    @Override
    public void getById(VehicleId request, StreamObserver<Vehicle> responseObserver) {

        UUID id = UUID.fromString(request.getId());
        LookupVehicleByIdCommand command = new LookupVehicleByIdCommand(id);
        ExecutionContext context = InternalExecutionContext.create();

        try {

            VehicleResult result = vehicleService.getById(command, context);
            Vehicle response = Vehicle.newBuilder()
                    .setId(result.id().toString())
                    .setVin(result.vin())
                    .setPlate(result.plate())
                    .setModel(result.model())
                    .setManufacturer(result.manufacturer())
                    .setYear(result.year())
                    .setOwnerId(result.ownerId().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (VehicleNotFoundException e) {
            StatusRuntimeException exception = new StatusRuntimeException(Status.NOT_FOUND.withDescription(e.getMessage()));
            responseObserver.onError(exception);
        }

    }

}
