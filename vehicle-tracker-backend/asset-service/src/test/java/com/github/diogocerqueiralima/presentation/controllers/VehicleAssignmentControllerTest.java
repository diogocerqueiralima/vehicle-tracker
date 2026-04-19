package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.application.ports.inbound.VehicleAssignmentUseCase;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.domain.assignments.VehicleRemovalReason;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.AssignDeviceToVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.UnassignDeviceFromVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.VehicleAssignmentDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
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
class VehicleAssignmentControllerTest {

    @Mock
    private VehicleAssignmentUseCase vehicleAssignmentUseCase;

    @InjectMocks
    private VehicleAssignmentController vehicleAssignmentController;

    @Test
    @DisplayName("Should assign device to vehicle and return created response")
    void should_assign_device_to_vehicle_and_return_created_response() {

        UUID userId = UUID.randomUUID();
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

        when(vehicleAssignmentUseCase.assignDeviceToVehicle(any())).thenReturn(result);

        AssignDeviceToVehicleRequestDTO request = new AssignDeviceToVehicleRequestDTO(
                deviceId,
                vehicleId,
                installedBy,
                "Installed in workshop A"
        );

        ResponseEntity<ApiResponseDTO<VehicleAssignmentDTO>> response = vehicleAssignmentController.assignDeviceToVehicle(
                buildAuthentication(userId),
                request
        );

        ArgumentCaptor<AssignDeviceToVehicleCommand> commandCaptor = ArgumentCaptor.forClass(AssignDeviceToVehicleCommand.class);
        verify(vehicleAssignmentUseCase).assignDeviceToVehicle(commandCaptor.capture());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Device assigned to vehicle successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().deviceId()).isEqualTo(deviceId);
        assertThat(response.getBody().data().vehicleId()).isEqualTo(vehicleId);
        assertThat(response.getBody().data().active()).isTrue();
        assertThat(commandCaptor.getValue().assignedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should unassign device from vehicle and return ok response")
    void should_unassign_device_from_vehicle_and_return_ok_response() {

        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        Instant assignedAt = Instant.parse("2026-03-20T12:00:00Z");

        VehicleAssignmentResult result = new VehicleAssignmentResult(
                deviceId,
                vehicleId,
                assignedAt,
                assignedBy,
                Instant.parse("2026-04-01T08:00:00Z"),
                UUID.randomUUID(),
                VehicleRemovalReason.RETIRED,
                null,
                "Uninstalled in workshop B",
                false
        );

        when(vehicleAssignmentUseCase.unassignDeviceFromVehicle(any())).thenReturn(result);

        UnassignDeviceFromVehicleRequestDTO request = new UnassignDeviceFromVehicleRequestDTO(
                deviceId,
                vehicleId,
                VehicleRemovalReason.RETIRED
        );

        ResponseEntity<ApiResponseDTO<VehicleAssignmentDTO>> response = vehicleAssignmentController.unassignDeviceFromVehicle(
                buildAuthentication(userId),
                request
        );

        ArgumentCaptor<UnassignDeviceFromVehicleCommand> commandCaptor = ArgumentCaptor.forClass(UnassignDeviceFromVehicleCommand.class);
        verify(vehicleAssignmentUseCase).unassignDeviceFromVehicle(commandCaptor.capture());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Device unassigned from vehicle successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().deviceId()).isEqualTo(deviceId);
        assertThat(response.getBody().data().vehicleId()).isEqualTo(vehicleId);
        assertThat(response.getBody().data().unassignedAt()).isNotNull();
        assertThat(response.getBody().data().unassignedBy()).isNotNull();
        assertThat(response.getBody().data().removalReason()).isEqualTo(VehicleRemovalReason.RETIRED);
        assertThat(response.getBody().data().active()).isFalse();
        assertThat(commandCaptor.getValue().unassignedBy()).isEqualTo(userId);
    }

    private JwtAuthenticationToken buildAuthentication(UUID userId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId.toString())
                .claims(claims -> claims.putAll(Map.of("realm_access", Map.of("roles", List.of()))))
                .build();

        return new JwtAuthenticationToken(jwt);
    }

}
