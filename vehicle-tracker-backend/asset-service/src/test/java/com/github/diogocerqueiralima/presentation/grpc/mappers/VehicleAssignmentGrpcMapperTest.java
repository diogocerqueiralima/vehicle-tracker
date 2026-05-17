package com.github.diogocerqueiralima.presentation.grpc.mappers;

import com.github.diogocerqueiralima.application.commands.GetVehicleAssignmentByDeviceIdCommand;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.proto.DeviceId;
import com.github.diogocerqueiralima.proto.VehicleAssignmentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(command.deviceId()).isEqualTo(deviceId);
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

        assertThat(response.getVehicleId()).isEqualTo(vehicleId.toString());
    }

}