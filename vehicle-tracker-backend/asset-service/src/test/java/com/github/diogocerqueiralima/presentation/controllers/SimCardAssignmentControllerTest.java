package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromSimCardCommand;
import com.github.diogocerqueiralima.application.ports.inbound.SimCardAssignmentUseCase;
import com.github.diogocerqueiralima.application.results.SimCardAssignmentResult;
import com.github.diogocerqueiralima.domain.assignments.SimCardRemovalReason;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.AssignDeviceToSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.SimCardAssignmentDTO;
import com.github.diogocerqueiralima.presentation.dto.UnassignDeviceFromSimCardRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimCardAssignmentControllerTest {

    @Mock
    private SimCardAssignmentUseCase simCardAssignmentUseCase;

    @InjectMocks
    private SimCardAssignmentController simCardAssignmentController;

    @Test
    @DisplayName("Should assign device to SIM card and return created response")
    void should_assign_device_to_sim_card_and_return_created_response() {

        UUID userId = UUID.randomUUID();
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

        when(simCardAssignmentUseCase.assignDeviceToSimCard(any())).thenReturn(result);

        AssignDeviceToSimCardRequestDTO request = new AssignDeviceToSimCardRequestDTO(
                deviceId,
                simCardId
        );

        ResponseEntity<ApiResponseDTO<SimCardAssignmentDTO>> response = simCardAssignmentController.assignDeviceToSimCard(
                buildAuthentication(userId),
                request
        );

        ArgumentCaptor<AssignDeviceToSimCardCommand> commandCaptor = ArgumentCaptor.forClass(AssignDeviceToSimCardCommand.class);
        verify(simCardAssignmentUseCase).assignDeviceToSimCard(commandCaptor.capture());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Device assigned to SIM card successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().deviceId()).isEqualTo(deviceId);
        assertThat(response.getBody().data().simCardId()).isEqualTo(simCardId);
        assertThat(response.getBody().data().active()).isTrue();
        assertThat(commandCaptor.getValue().assignedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should unassign device from SIM card and return ok response")
    void should_unassign_device_from_sim_card_and_return_ok_response() {

        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        UUID simCardId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        Instant assignedAt = Instant.parse("2026-03-20T12:00:00Z");

        SimCardAssignmentResult result = new SimCardAssignmentResult(
                deviceId,
                simCardId,
                assignedAt,
                assignedBy,
                Instant.parse("2026-04-01T08:00:00Z"),
                UUID.randomUUID(),
                SimCardRemovalReason.UPGRADE,
                false
        );

        when(simCardAssignmentUseCase.unassignDeviceFromSimCard(any())).thenReturn(result);

        UnassignDeviceFromSimCardRequestDTO request = new UnassignDeviceFromSimCardRequestDTO(
                deviceId,
                simCardId,
                SimCardRemovalReason.UPGRADE
        );

        ResponseEntity<ApiResponseDTO<SimCardAssignmentDTO>> response = simCardAssignmentController.unassignDeviceFromSimCard(
                buildAuthentication(userId),
                request
        );

        ArgumentCaptor<UnassignDeviceFromSimCardCommand> commandCaptor = ArgumentCaptor.forClass(UnassignDeviceFromSimCardCommand.class);
        verify(simCardAssignmentUseCase).unassignDeviceFromSimCard(commandCaptor.capture());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Device unassigned from SIM card successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().deviceId()).isEqualTo(deviceId);
        assertThat(response.getBody().data().simCardId()).isEqualTo(simCardId);
        assertThat(response.getBody().data().unassignedAt()).isNotNull();
        assertThat(response.getBody().data().unassignedBy()).isNotNull();
        assertThat(response.getBody().data().removalReason()).isEqualTo(SimCardRemovalReason.UPGRADE);
        assertThat(response.getBody().data().active()).isFalse();
        assertThat(commandCaptor.getValue().unassignedBy()).isEqualTo(userId);
    }

    private JwtAuthenticationToken buildAuthentication(UUID userId) {
        Jwt jwt = Jwt.withTokenValue("test-jwt-token")
                .header("alg", "none")
                .claim("sub", userId.toString())
                .claims(claims -> claims.putAll(Map.of("realm_access", Map.of("roles", List.of()))))
                .build();

        return new JwtAuthenticationToken(jwt);
    }

}
