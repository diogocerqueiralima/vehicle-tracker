package com.github.diogocerqueiralima.asset.service.presentation.http.mappers;

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleAssignmentHistoryCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleRemovalReason;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.AssignDeviceToVehicleRequestDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.PageDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UnassignDeviceFromVehicleRequestDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.VehicleAssignmentDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleAssignmentHttpMapperTest {

    @Test
    @DisplayName("Should map assign request to command")
    void should_map_assign_request_to_command() {

        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        UUID installedBy = UUID.randomUUID();

        AssignDeviceToVehicleRequestDTO request = new AssignDeviceToVehicleRequestDTO(
                deviceId,
                installedBy,
                "Installed in workshop A"
        );

        AssignDeviceToVehicleCommand command = VehicleAssignmentHttpMapper.toAssignDeviceToVehicleCommand(
                request, vehicleId, assignedBy
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

        VehicleAssignmentDTO dto = VehicleAssignmentHttpMapper.toDTO(result);

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
                VehicleRemovalReason.LOSS
        );

        UnassignDeviceFromVehicleCommand command = VehicleAssignmentHttpMapper.toUnassignDeviceFromVehicleCommand(
                request,
                vehicleId,
                unassignedBy
        );

        assertThat(command.deviceId()).isEqualTo(deviceId);
        assertThat(command.vehicleId()).isEqualTo(vehicleId);
        assertThat(command.unassignedBy()).isEqualTo(unassignedBy);
        assertThat(command.removalReason()).isEqualTo(VehicleRemovalReason.LOSS);
    }

    @Test
    @DisplayName("Should map vehicle id, user id and pagination params to get history command")
    void should_map_vehicle_id_user_id_and_pagination_params_to_get_history_command() {

        UUID vehicleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        GetVehicleAssignmentHistoryCommand command = VehicleAssignmentHttpMapper.toGetVehicleAssignmentHistoryCommand(
                vehicleId, userId, 2, 20
        );

        assertThat(command.vehicleId()).isEqualTo(vehicleId);
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.pageNumber()).isEqualTo(2);
        assertThat(command.pageSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("Should map page result to page dto with mapped assignment dtos")
    void should_map_page_result_to_page_dto_with_mapped_assignment_dtos() {

        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        Instant assignedAt = Instant.parse("2026-03-20T12:00:00Z");

        VehicleAssignmentResult result = new VehicleAssignmentResult(
                deviceId,
                vehicleId,
                assignedAt,
                assignedBy,
                null,
                null,
                null,
                null,
                null,
                true
        );

        PageResult<VehicleAssignmentResult> pageResult = new PageResult<>(1, 10, 3, 25L, List.of(result));

        PageDTO<VehicleAssignmentDTO> dto = VehicleAssignmentHttpMapper.toPageDTO(pageResult);

        assertThat(dto.pageNumber()).isEqualTo(1);
        assertThat(dto.pageSize()).isEqualTo(10);
        assertThat(dto.totalPages()).isEqualTo(3);
        assertThat(dto.totalElements()).isEqualTo(25L);
        assertThat(dto.data()).hasSize(1);
        assertThat(dto.data().getFirst().deviceId()).isEqualTo(deviceId);
        assertThat(dto.data().getFirst().vehicleId()).isEqualTo(vehicleId);
        assertThat(dto.data().getFirst().assignedAt()).isEqualTo(assignedAt);
        assertThat(dto.data().getFirst().assignedBy()).isEqualTo(assignedBy);
        assertThat(dto.data().getFirst().active()).isTrue();
    }

}
