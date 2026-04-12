package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.application.ports.inbound.VehicleUseCase;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.CreateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.PageDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.VehicleDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock
    private VehicleUseCase vehicleUseCase;

    @InjectMocks
    private VehicleController vehicleController;

    @Test
    @DisplayName("Should create vehicle and return created response")
    void should_create_vehicle_and_return_created_response() {
        UUID userId = UUID.randomUUID();

        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        VehicleResult result = new VehicleResult(
                id,
                now,
                now,
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        when(vehicleUseCase.create(any())).thenReturn(result);

        CreateVehicleRequestDTO request = new CreateVehicleRequestDTO(
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        ResponseEntity<ApiResponseDTO<VehicleDTO>> response = vehicleController.create(buildJwt(userId), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Vehicle created successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().id()).isEqualTo(id);
        assertThat(response.getBody().data().vin()).isEqualTo("1HGCM82633A123456");
        assertThat(response.getBody().data().plate()).isEqualTo("AA-00-AA");
    }

    @Test
    @DisplayName("Should map response data from use case result")
    void should_map_response_data_from_use_case_result() {
        UUID userId = UUID.randomUUID();

        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        VehicleResult result = new VehicleResult(
                id,
                now,
                now,
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        when(vehicleUseCase.create(any())).thenReturn(result);

        CreateVehicleRequestDTO request = new CreateVehicleRequestDTO(
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        ResponseEntity<ApiResponseDTO<VehicleDTO>> response = vehicleController.create(buildJwt(userId), request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().model()).isEqualTo("Model 3");
        assertThat(response.getBody().data().manufacturer()).isEqualTo("Tesla");
        assertThat(response.getBody().data().manufacturingDate()).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test
    @DisplayName("Should update vehicle and return ok response")
    void should_update_vehicle_and_return_ok_response() {
        UUID userId = UUID.randomUUID();

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-15T12:00:00Z");
        Instant updatedAt = Instant.parse("2026-03-16T12:00:00Z");

        VehicleResult result = new VehicleResult(
                id,
                createdAt,
                updatedAt,
                "1HGCM82633A123456",
                "BB-11-BB",
                "Model Y",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        when(vehicleUseCase.update(any())).thenReturn(result);

        UpdateVehicleRequestDTO request = new UpdateVehicleRequestDTO(
                "1HGCM82633A123456",
                "BB-11-BB",
                "Model Y",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        ResponseEntity<ApiResponseDTO<VehicleDTO>> response = vehicleController.update(id, buildJwt(userId), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Vehicle updated successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().id()).isEqualTo(id);
        assertThat(response.getBody().data().plate()).isEqualTo("BB-11-BB");
        assertThat(response.getBody().data().model()).isEqualTo("Model Y");
    }

    @Test
    @DisplayName("Should get vehicle by id and return ok response")
    void should_get_vehicle_by_id_and_return_ok_response() {
        UUID userId = UUID.randomUUID();

        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        VehicleResult result = new VehicleResult(
                id,
                now,
                now,
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        when(vehicleUseCase.getById(any())).thenReturn(result);

        ResponseEntity<ApiResponseDTO<VehicleDTO>> response = vehicleController.getById(id, buildJwt(userId));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Vehicle fetched successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().id()).isEqualTo(id);
        assertThat(response.getBody().data().vin()).isEqualTo("1HGCM82633A123456");
    }

    @Test
    @DisplayName("Should get vehicle page and return ok response")
    void should_get_vehicle_page_and_return_ok_response() {
        UUID userId = UUID.randomUUID();

        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");
        VehicleResult vehicleResult = new VehicleResult(
                id,
                now,
                now,
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        when(vehicleUseCase.getPage(any())).thenReturn(new PageResult<>(1, 10, 1, 1, List.of(vehicleResult)));

        ResponseEntity<ApiResponseDTO<PageDTO<VehicleDTO>>> response = vehicleController.getPage(buildJwt(userId), 1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Vehicles fetched successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().pageNumber()).isEqualTo(1);
        assertThat(response.getBody().data().pageSize()).isEqualTo(10);
        assertThat(response.getBody().data().data()).hasSize(1);
        assertThat(response.getBody().data().data().getFirst().id()).isEqualTo(id);
    }

    private Jwt buildJwt(UUID userId) {
        return new Jwt(
                "token-value",
                Instant.parse("2026-03-15T12:00:00Z"),
                Instant.parse("2026-03-16T12:00:00Z"),
                Map.of("alg", "none"),
                Map.of("sub", userId.toString())
        );
    }

}


