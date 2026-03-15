package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.ports.inbound.VehicleUseCase;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.CreateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.VehicleDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
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
    void shouldCreateVehicleAndReturnCreatedResponse() {

        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        VehicleResult result = new VehicleResult(
                id,
                now,
                now,
                "1HGCM82633A123456",
                "ABCD1234",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        when(vehicleUseCase.create(any())).thenReturn(result);

        CreateVehicleRequestDTO request = new CreateVehicleRequestDTO(
                "1HGCM82633A123456",
                "ABCD1234",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        ResponseEntity<ApiResponseDTO<VehicleDTO>> response = vehicleController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Vehicle created successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().id()).isEqualTo(id);
        assertThat(response.getBody().data().vin()).isEqualTo("1HGCM82633A123456");
        assertThat(response.getBody().data().plate()).isEqualTo("ABCD1234");
    }

    @Test
    void shouldMapResponseDataFromUseCaseResult() {

        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        VehicleResult result = new VehicleResult(
                id,
                now,
                now,
                "1HGCM82633A123456",
                "ABCD1234",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        when(vehicleUseCase.create(any())).thenReturn(result);

        CreateVehicleRequestDTO request = new CreateVehicleRequestDTO(
                "1HGCM82633A123456",
                "ABCD1234",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        ResponseEntity<ApiResponseDTO<VehicleDTO>> response = vehicleController.create(request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().model()).isEqualTo("Model 3");
        assertThat(response.getBody().data().manufacturer()).isEqualTo("Tesla");
        assertThat(response.getBody().data().manufacturingDate()).isEqualTo(LocalDate.of(2024, 1, 15));
    }

}


