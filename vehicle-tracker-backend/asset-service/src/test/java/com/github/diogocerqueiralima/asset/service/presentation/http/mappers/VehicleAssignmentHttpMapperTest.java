package com.github.diogocerqueiralima.asset.service.presentation.http.mappers;

import com.github.diogocerqueiralima.api.common.dto.PageDTO;
import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleAssignmentHistoryCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleRemovalReason;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.AssignDeviceToVehicleRequestDTO;
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

        AssignDeviceToVehicleCommand command = VehicleAssignmentHttpMapper.INSTANCE.toAssignDeviceToVehicleCommand(
                request, vehicleId, assignedBy
        );

        assertThat(command.getDeviceId()).isEqualTo(deviceId);
        assertThat(command.getVehicleId()).isEqualTo(vehicleId);
        assertThat(command.getAssignedBy()).isEqualTo(assignedBy);
        assertThat(command.getInstalledBy()).isEqualTo(installedBy);
        assertThat(command.getNotes()).isEqualTo("Installed in workshop A");
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

        VehicleAssignmentDTO dto = VehicleAssignmentHttpMapper.INSTANCE.toDTO(result);

        assertThat(dto.getDeviceId()).isEqualTo(deviceId);
        assertThat(dto.getVehicleId()).isEqualTo(vehicleId);
        assertThat(dto.getAssignedAt()).isEqualTo(assignedAt);
        assertThat(dto.getAssignedBy()).isEqualTo(assignedBy);
        assertThat(dto.getUnassignedAt()).isNull();
        assertThat(dto.getUnassignedBy()).isNull();
        assertThat(dto.getRemovalReason()).isNull();
        assertThat(dto.getInstalledBy()).isEqualTo(installedBy);
        assertThat(dto.getNotes()).isEqualTo("Installed in workshop A");
        assertThat(dto.getActive()).isTrue();
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

        UnassignDeviceFromVehicleCommand command = VehicleAssignmentHttpMapper.INSTANCE.toUnassignDeviceFromVehicleCommand(
                request,
                vehicleId,
                unassignedBy
        );

        assertThat(command.getDeviceId()).isEqualTo(deviceId);
        assertThat(command.getVehicleId()).isEqualTo(vehicleId);
        assertThat(command.getUnassignedBy()).isEqualTo(unassignedBy);
        assertThat(command.getRemovalReason()).isEqualTo(VehicleRemovalReason.LOSS);
    }

    @Test
    @DisplayName("Should map vehicle id, user id and pagination params to get history command")
    void should_map_vehicle_id_user_id_and_pagination_params_to_get_history_command() {

        UUID vehicleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        GetVehicleAssignmentHistoryCommand command = VehicleAssignmentHttpMapper.INSTANCE.toGetVehicleAssignmentHistoryCommand(
                vehicleId, userId, 2, 20
        );

        assertThat(command.getVehicleId()).isEqualTo(vehicleId);
        assertThat(command.getUserId()).isEqualTo(userId);
        assertThat(command.getPageNumber()).isEqualTo(2);
        assertThat(command.getPageSize()).isEqualTo(20);
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

        PageDTO<VehicleAssignmentDTO> dto = VehicleAssignmentHttpMapper.INSTANCE.toPageDTO(pageResult);

        assertThat(dto.getPageNumber()).isEqualTo(1);
        assertThat(dto.getPageSize()).isEqualTo(10);
        assertThat(dto.getTotalPages()).isEqualTo(3);
        assertThat(dto.getTotalElements()).isEqualTo(25L);
        assertThat(dto.getData()).hasSize(1);
        assertThat(dto.getData().getFirst().getDeviceId()).isEqualTo(deviceId);
        assertThat(dto.getData().getFirst().getVehicleId()).isEqualTo(vehicleId);
        assertThat(dto.getData().getFirst().getAssignedAt()).isEqualTo(assignedAt);
        assertThat(dto.getData().getFirst().getAssignedBy()).isEqualTo(assignedBy);
        assertThat(dto.getData().getFirst().getActive()).isTrue();
    }

}
