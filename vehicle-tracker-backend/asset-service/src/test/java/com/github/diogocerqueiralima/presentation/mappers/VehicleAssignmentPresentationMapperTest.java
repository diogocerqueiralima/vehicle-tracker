package com.github.diogocerqueiralima.presentation.mappers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.domain.assignments.VehicleRemovalReason;
import com.github.diogocerqueiralima.presentation.dto.AssignDeviceToVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.UnassignDeviceFromVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.VehicleAssignmentDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleAssignmentPresentationMapperTest {

    @Test
    @DisplayName("Should map assign request to command")
    void should_map_assign_request_to_command() {

        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        UUID installedBy = UUID.randomUUID();

        AssignDeviceToVehicleRequestDTO request = new AssignDeviceToVehicleRequestDTO(
                deviceId,
                vehicleId,
                installedBy,
                "Installed in workshop A"
        );

        AssignDeviceToVehicleCommand command = VehicleAssignmentPresentationMapper.toAssignDeviceToVehicleCommand(
                request, assignedBy
        );

        assertThat(command.deviceId()).isEqualTo(deviceId);
        assertThat(command.vehicleId()).isEqualTo(vehicleId);
        assertThat(command.assignedBy()).isEqualTo(assignedBy);
        assertThat(command.installedBy()).isEqualTo(installedBy);
        assertThat(command.notes()).isEqualTo("Installed in workshop A");
    }

    @Test
    @DisplayName("Should map assignment result to dto")
    void should_map_assignment_result_to_dto() {

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

        VehicleAssignmentDTO dto = VehicleAssignmentPresentationMapper.toDTO(result);

        assertThat(dto.deviceId()).isEqualTo(deviceId);
        assertThat(dto.vehicleId()).isEqualTo(vehicleId);
        assertThat(dto.assignedAt()).isEqualTo(assignedAt);
        assertThat(dto.assignedBy()).isEqualTo(assignedBy);
        assertThat(dto.unassignedAt()).isNull();
        assertThat(dto.unassignedBy()).isNull();
        assertThat(dto.removalReason()).isNull();
        assertThat(dto.installedBy()).isEqualTo(installedBy);
        assertThat(dto.notes()).isEqualTo("Installed in workshop A");
        assertThat(dto.active()).isTrue();
    }

    @Test
    @DisplayName("Should map unassign request to command")
    void should_map_unassign_request_to_command() {

        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID unassignedBy = UUID.randomUUID();

        UnassignDeviceFromVehicleRequestDTO request = new UnassignDeviceFromVehicleRequestDTO(
                deviceId,
                vehicleId,
                VehicleRemovalReason.LOSS
        );

        UnassignDeviceFromVehicleCommand command = VehicleAssignmentPresentationMapper.toUnassignDeviceFromVehicleCommand(
                request,
                unassignedBy
        );

        assertThat(command.deviceId()).isEqualTo(deviceId);
        assertThat(command.vehicleId()).isEqualTo(vehicleId);
        assertThat(command.unassignedBy()).isEqualTo(unassignedBy);
        assertThat(command.removalReason()).isEqualTo(VehicleRemovalReason.LOSS);
    }

}

