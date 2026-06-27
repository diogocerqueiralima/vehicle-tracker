package com.github.diogocerqueiralima.asset.service.presentation.grpc.mappers;

import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleAssignmentByDeviceIdCommand;
import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleRemovalReason;
import com.github.diogocerqueiralima.schema.proto.DeviceId;
import com.github.diogocerqueiralima.schema.proto.VehicleAssignmentResponse;
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
            builder.setRemovalReason(toProtoRemovalReason(result.removalReason()));
        }

        if (result.installedBy() != null) {
            builder.setInstalledBy(result.installedBy().toString());
        }

        if (result.notes() != null) {
            builder.setNotes(result.notes());
        }

        return builder.build();
    }

    private static com.github.diogocerqueiralima.schema.proto.VehicleRemovalReason toProtoRemovalReason(
            VehicleRemovalReason removalReason
    ) {
        return switch (removalReason) {
            case UPGRADE -> com.github.diogocerqueiralima.schema.proto.VehicleRemovalReason.VEHICLE_REMOVAL_REASON_UPGRADE;
            case LOSS -> com.github.diogocerqueiralima.schema.proto.VehicleRemovalReason.VEHICLE_REMOVAL_REASON_LOSS;
            case SOLD -> com.github.diogocerqueiralima.schema.proto.VehicleRemovalReason.VEHICLE_REMOVAL_REASON_SOLD;
            case RETIRED -> com.github.diogocerqueiralima.schema.proto.VehicleRemovalReason.VEHICLE_REMOVAL_REASON_RETIRED;
            case OTHER -> com.github.diogocerqueiralima.schema.proto.VehicleRemovalReason.VEHICLE_REMOVAL_REASON_OTHER;
        };
    }

}
