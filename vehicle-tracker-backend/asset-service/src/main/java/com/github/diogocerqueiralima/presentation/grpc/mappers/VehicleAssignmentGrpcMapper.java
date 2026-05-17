package com.github.diogocerqueiralima.presentation.grpc.mappers;

import com.github.diogocerqueiralima.application.commands.GetVehicleAssignmentByDeviceIdCommand;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.proto.DeviceId;
import com.github.diogocerqueiralima.proto.VehicleAssignmentResponse;

import java.util.UUID;

/**
 * Mapper for vehicle assignment conversions in the gRPC presentation layer.
 */
public final class VehicleAssignmentGrpcMapper {

    // Should not be instantiated
    private VehicleAssignmentGrpcMapper() {}

    /**
     * Builds a query command from a gRPC request message.
     *
     * @param request gRPC message carrying the device identifier.
     * @return command consumed by the application layer.
     */
    public static GetVehicleAssignmentByDeviceIdCommand toGetVehicleAssignmentByDeviceIdCommand(DeviceId request) {
        return new GetVehicleAssignmentByDeviceIdCommand(UUID.fromString(request.getId()));
    }

    /**
     * Builds a gRPC response message from an application result.
     *
     * @param result vehicle assignment result from the application layer.
     * @return gRPC response carrying the assigned vehicle identifier.
     */
    public static VehicleAssignmentResponse toResponse(VehicleAssignmentResult result) {
        return VehicleAssignmentResponse.newBuilder()
                .setVehicleId(result.vehicleId().toString())
                .build();
    }

}