package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.ports.inbound.DeviceUseCase;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.CreateDeviceRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.DeviceDTO;
import com.github.diogocerqueiralima.presentation.dto.PageDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateDeviceRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        DeviceResult result = new DeviceResult(
                id,
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
                "123456789012345"
        );

        ResponseEntity<ApiResponseDTO<DeviceDTO>> response = deviceController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Device created successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().id()).isEqualTo(id);
        assertThat(response.getBody().data().serialNumber()).isEqualTo("SN-001");
        assertThat(response.getBody().data().imei()).isEqualTo("123456789012345");
    }

    @Test
    @DisplayName("Should map response data from use case result")
    void should_map_response_data_from_use_case_result() {

        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        DeviceResult result = new DeviceResult(
                id,
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
                "123456789012345"
        );

        ResponseEntity<ApiResponseDTO<DeviceDTO>> response = deviceController.create(request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().model()).isEqualTo("TK-1000");
        assertThat(response.getBody().data().manufacturer()).isEqualTo("Teltonika");
    }

    @Test
    @DisplayName("Should update device and return ok response")
    void should_update_device_and_return_ok_response() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-15T12:00:00Z");
        Instant updatedAt = Instant.parse("2026-03-16T12:00:00Z");

        DeviceResult result = new DeviceResult(
                id,
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
                "223456789012345"
        );

        ResponseEntity<ApiResponseDTO<DeviceDTO>> response = deviceController.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Device updated successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().id()).isEqualTo(id);
        assertThat(response.getBody().data().serialNumber()).isEqualTo("SN-002");
        assertThat(response.getBody().data().model()).isEqualTo("TK-1100");
    }

    @Test
    @DisplayName("Should get device by id and return ok response")
    void should_get_device_by_id_and_return_ok_response() {

        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        DeviceResult result = new DeviceResult(
                id,
                now,
                now,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        when(deviceUseCase.getById(any())).thenReturn(result);

        ResponseEntity<ApiResponseDTO<DeviceDTO>> response = deviceController.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Device fetched successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().id()).isEqualTo(id);
        assertThat(response.getBody().data().serialNumber()).isEqualTo("SN-001");
    }

    @Test
    @DisplayName("Should get device page and return ok response")
    void should_get_device_page_and_return_ok_response() {

        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");
        DeviceResult deviceResult = new DeviceResult(
                id,
                now,
                now,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        when(deviceUseCase.getPage(any())).thenReturn(new PageResult<>(1, 10, 1, 1, List.of(deviceResult)));

        ResponseEntity<ApiResponseDTO<PageDTO<DeviceDTO>>> response = deviceController.getPage(1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Devices fetched successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().pageNumber()).isEqualTo(1);
        assertThat(response.getBody().data().pageSize()).isEqualTo(10);
        assertThat(response.getBody().data().data()).hasSize(1);
        assertThat(response.getBody().data().data().getFirst().id()).isEqualTo(id);
    }

}

