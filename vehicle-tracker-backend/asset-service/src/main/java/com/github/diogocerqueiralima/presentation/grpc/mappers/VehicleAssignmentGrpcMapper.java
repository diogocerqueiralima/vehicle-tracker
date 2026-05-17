package com.github.diogocerqueiralima.presentation.grpc.mappers;

import com.github.diogocerqueiralima.application.commands.GetVehicleAssignmentByDeviceIdCommand;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.proto.DeviceId;
import com.github.diogocerqueiralima.proto.VehicleAssignmentResponse;
import com.github.diogocerqueiralima.proto.VehicleRemovalReason;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;

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
        VehicleAssignmentResponse.Builder builder = VehicleAssignmentResponse.newBuilder()
                .setVehicleId(result.vehicleId().toString())
                .setDeviceId(result.deviceId().toString())
                .setAssignedAt(Timestamps.fromMillis(result.assignedAt().toEpochMilli()))
                .setAssignedBy(result.assignedBy().toString())
                .setActive(result.active());

        if (result.unassignedAt() != null) {
            builder.setUnassignedAt(Timestamps.fromMillis(result.unassignedAt().toEpochMilli()));
        }

        if (result.unassignedBy() != null) {
            builder.setUnassignedBy(result.unassignedBy().toString());
        }

        if (result.removalReason() != null) {
            builder.setRemovalReason(VehicleRemovalReason.valueOf(result.removalReason().name()));
        }

        if (result.installedBy() != null) {
            builder.setInstalledBy(result.installedBy().toString());
        }

        if (result.notes() != null) {
            builder.setNotes(result.notes());
        }

        return builder.build();
    }

}