package com.github.diogocerqueiralima.presentation.http.controllers;

import com.github.diogocerqueiralima.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.domain.ports.inbound.DeviceUseCase;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.presentation.http.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.http.dto.CreateDeviceRequestDTO;
import com.github.diogocerqueiralima.presentation.http.dto.DeviceDTO;
import com.github.diogocerqueiralima.presentation.http.dto.PageDTO;
import com.github.diogocerqueiralima.presentation.http.dto.UpdateDeviceRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {

    @Mock
    private DeviceUseCase deviceUseCase;

    @InjectMocks
    private DeviceController deviceController;

    @Test
    @DisplayName("Should create device and return created response")
    void should_create_device_and_return_created_response() {

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        DeviceResult result = new DeviceResult(
                id,
                ownerId,
                now,
                now,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        when(deviceUseCase.create(any())).thenReturn(result);

        CreateDeviceRequestDTO request = new CreateDeviceRequestDTO(
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345",
                ownerId
        );

        ResponseEntity<ApiResponseDTO<DeviceDTO>> response = deviceController.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Device created successfully.", response.getBody().message());
        assertNotNull(response.getBody().data());
        assertEquals(id, response.getBody().data().id());
        assertEquals("SN-001", response.getBody().data().serialNumber());
        assertEquals("123456789012345", response.getBody().data().imei());
    }

    @Test
    @DisplayName("Should map response data from use case result")
    void should_map_response_data_from_use_case_result() {

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        DeviceResult result = new DeviceResult(
                id,
                ownerId,
                now,
                now,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        when(deviceUseCase.create(any())).thenReturn(result);

        CreateDeviceRequestDTO request = new CreateDeviceRequestDTO(
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345",
                ownerId
        );

        ResponseEntity<ApiResponseDTO<DeviceDTO>> response = deviceController.create(request);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().data());
        assertEquals("TK-1000", response.getBody().data().model());
        assertEquals("Teltonika", response.getBody().data().manufacturer());
    }

    @Test
    @DisplayName("Should update device and return ok response")
    void should_update_device_and_return_ok_response() {

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-15T12:00:00Z");
        Instant updatedAt = Instant.parse("2026-03-16T12:00:00Z");

        DeviceResult result = new DeviceResult(
                id,
                ownerId,
                createdAt,
                updatedAt,
                "SN-002",
                "TK-1100",
                "Teltonika",
                "223456789012345"
        );

        when(deviceUseCase.update(any())).thenReturn(result);

        UpdateDeviceRequestDTO request = new UpdateDeviceRequestDTO(
                "SN-002",
                "TK-1100",
                "Teltonika",
                "223456789012345",
                ownerId
        );

        ResponseEntity<ApiResponseDTO<DeviceDTO>> response = deviceController.update(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Device updated successfully.", response.getBody().message());
        assertNotNull(response.getBody().data());
        assertEquals(id, response.getBody().data().id());
        assertEquals("SN-002", response.getBody().data().serialNumber());
        assertEquals("TK-1100", response.getBody().data().model());
    }

    @Test
    @DisplayName("Should get device by id and return ok response")
    void should_get_device_by_id_and_return_ok_response() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        DeviceResult result = new DeviceResult(
                id,
                userId,
                now,
                now,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        when(deviceUseCase.getById(any())).thenReturn(result);

        ResponseEntity<ApiResponseDTO<DeviceDTO>> response = deviceController.getById(id, buildAuthentication(userId));

        ArgumentCaptor<GetDeviceByIdCommand> commandCaptor = ArgumentCaptor.forClass(GetDeviceByIdCommand.class);
        verify(deviceUseCase).getById(commandCaptor.capture());
        GetDeviceByIdCommand capturedCommand = commandCaptor.getValue();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Device fetched successfully.", response.getBody().message());
        assertNotNull(response.getBody().data());
        assertEquals(id, response.getBody().data().id());
        assertEquals("SN-001", response.getBody().data().serialNumber());
        assertEquals(id, capturedCommand.id());
        assertEquals(userId, capturedCommand.userId());
        assertFalse(capturedCommand.isAdmin());
    }

    @Test
    @DisplayName("Should get device page and return ok response")
    void should_get_device_page_and_return_ok_response() {
        UUID userId = UUID.randomUUID();

        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");
        DeviceResult deviceResult = new DeviceResult(
                id,
                new UUID(0, 0),
                now,
                now,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        when(deviceUseCase.getPage(any())).thenReturn(new PageResult<>(1, 10, 1, 1, List.of(deviceResult)));

        ResponseEntity<ApiResponseDTO<PageDTO<DeviceDTO>>> response = deviceController.getPage(buildAuthentication(userId), 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Devices fetched successfully.", response.getBody().message());
        assertNotNull(response.getBody().data());
        assertEquals(1, response.getBody().data().pageNumber());
        assertEquals(10, response.getBody().data().pageSize());
        assertEquals(1, response.getBody().data().data().size());
        assertEquals(id, response.getBody().data().data().getFirst().id());
    }

    private JwtAuthenticationToken buildAuthentication(UUID userId) {
        return buildAuthentication(userId, List.of());
    }

    private JwtAuthenticationToken buildAuthentication(UUID userId, List<String> roles) {
        Jwt jwt = new Jwt(
                "token-value",
                Instant.parse("2026-03-15T12:00:00Z"),
                Instant.parse("2026-03-16T12:00:00Z"),
                Map.of("alg", "none"),
                Map.of("sub", userId.toString())
        );

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> "ROLE_" + role.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        return new JwtAuthenticationToken(jwt, authorities, userId.toString());
    }

}