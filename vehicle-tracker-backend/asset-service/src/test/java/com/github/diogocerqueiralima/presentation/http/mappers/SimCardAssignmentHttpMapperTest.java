package com.github.diogocerqueiralima.presentation.http.mappers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardAssignmentResult;
import com.github.diogocerqueiralima.domain.assignments.SimCardRemovalReason;
import com.github.diogocerqueiralima.presentation.http.dto.AssignDeviceToSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.http.dto.SimCardAssignmentDTO;
import com.github.diogocerqueiralima.presentation.http.dto.UnassignDeviceFromSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.http.mappers.SimCardAssignmentHttpMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimCardAssignmentHttpMapperTest {

    @Test
    @DisplayName("Should map assign request to command")
    void should_map_assign_request_to_command() {

        UUID deviceId = UUID.randomUUID();
        UUID simCardId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();

        AssignDeviceToSimCardRequestDTO request = new AssignDeviceToSimCardRequestDTO(
                deviceId
        );

        AssignDeviceToSimCardCommand command = SimCardAssignmentHttpMapper.toAssignDeviceToSimCardCommand(
                request,
                simCardId,
                assignedBy
        );

        assertThat(command.deviceId()).isEqualTo(deviceId);
        assertThat(command.simCardId()).isEqualTo(simCardId);
        assertThat(command.assignedBy()).isEqualTo(assignedBy);
    }

    @Test
    @DisplayName("Should map assignment result to dto")
    void should_map_assignment_result_to_dto() {

        UUID deviceId = UUID.randomUUID();
        UUID simCardId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        Instant assignedAt = Instant.parse("2026-03-20T12:00:00Z");

        SimCardAssignmentResult result = new SimCardAssignmentResult(
                deviceId,
                simCardId,
                assignedAt,
                assignedBy,
                null,
                null,
                null,
                true
        );

        SimCardAssignmentDTO dto = SimCardAssignmentHttpMapper.toDTO(result);

        assertThat(dto.deviceId()).isEqualTo(deviceId);
        assertThat(dto.simCardId()).isEqualTo(simCardId);
        assertThat(dto.assignedAt()).isEqualTo(assignedAt);
        assertThat(dto.assignedBy()).isEqualTo(assignedBy);
        assertThat(dto.unassignedAt()).isNull();
        assertThat(dto.unassignedBy()).isNull();
        assertThat(dto.removalReason()).isNull();
        assertThat(dto.active()).isTrue();
    }

    @Test
    @DisplayName("Should map unassign request to command")
    void should_map_unassign_request_to_command() {

        UUID deviceId = UUID.randomUUID();
        UUID simCardId = UUID.randomUUID();
        UUID unassignedBy = UUID.randomUUID();

        UnassignDeviceFromSimCardRequestDTO request = new UnassignDeviceFromSimCardRequestDTO(
                deviceId,
                SimCardRemovalReason.UPGRADE
        );

        UnassignDeviceFromSimCardCommand command = SimCardAssignmentHttpMapper.toUnassignDeviceFromSimCardCommand(
                request,
                simCardId,
                unassignedBy
        );

        assertThat(command.deviceId()).isEqualTo(deviceId);
        assertThat(command.simCardId()).isEqualTo(simCardId);
        assertThat(command.unassignedBy()).isEqualTo(unassignedBy);
        assertThat(command.removalReason()).isEqualTo(SimCardRemovalReason.UPGRADE);
    }

}
