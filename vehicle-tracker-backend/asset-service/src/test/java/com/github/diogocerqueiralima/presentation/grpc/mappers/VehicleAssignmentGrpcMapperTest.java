package com.github.diogocerqueiralima.presentation.grpc.mappers;

import com.github.diogocerqueiralima.application.commands.GetVehicleAssignmentByDeviceIdCommand;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.proto.DeviceId;
import com.github.diogocerqueiralima.proto.VehicleAssignmentResponse;
import com.github.diogocerqueiralima.proto.VehicleRemovalReason;
import com.google.protobuf.util.Timestamps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VehicleAssignmentGrpcMapperTest {

    @Test
    @DisplayName("Should map DeviceId request to GetVehicleAssignmentByDeviceIdCommand")
    void should_map_device_id_request_to_command() {

        UUID deviceId = UUID.randomUUID();

        DeviceId request = DeviceId.newBuilder()
                .setId(deviceId.toString())
                .build();

        GetVehicleAssignmentByDeviceIdCommand command =
                VehicleAssignmentGrpcMapper.toGetVehicleAssignmentByDeviceIdCommand(request);

        assertEquals(deviceId, command.deviceId());
    }

    @Test
    @DisplayName("Should map VehicleAssignmentResult to VehicleAssignmentResponse")
    void should_map_vehicle_assignment_result_to_response() {

        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        UUID installedBy = UUID.randomUUID();
        Instant assignedAt = Instant.parse("2026-03-20T12:00:00Z");

        VehicleAssignmentResult result = new VehicleAssignmentResult(
                deviceId,
                vehicleId,
                assignedAt,
                assignedBy,
                null,
                null,
                null,
                installedBy,
                "Installed in workshop A",
                true
        );

        VehicleAssignmentResponse response = VehicleAssignmentGrpcMapper.toResponse(result);

        assertEquals(vehicleId.toString(), response.getVehicleId());
        assertEquals(deviceId.toString(), response.getDeviceId());
        assertEquals(installedBy.toString(), response.getInstalledBy());
        assertEquals(assignedAt.toEpochMilli(), Timestamps.toMillis(response.getAssignedAt()));
        assertEquals(assignedBy.toString(), response.getAssignedBy());
        assertFalse(response.hasUnassignedAt());
        assertFalse(response.hasUnassignedBy());
        assertFalse(response.hasRemovalReason());
        assertEquals(installedBy.toString(), response.getInstalledBy());
        assertTrue(response.getActive());
        assertEquals(result.notes(), response.getNotes());

    }

}
