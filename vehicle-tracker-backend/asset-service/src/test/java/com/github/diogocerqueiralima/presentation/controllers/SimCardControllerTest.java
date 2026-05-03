package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.ports.inbound.SimCardUseCase;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.CreateSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.SimCardDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateSimCardRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimCardControllerTest {

    @Mock
    private SimCardUseCase simCardUseCase;

    @InjectMocks
    private SimCardController simCardController;

    @Test
    @DisplayName("Should create sim card and return created response")
    void should_create_sim_card_and_return_created_response() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        SimCardResult result = new SimCardResult(id, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001");
        when(simCardUseCase.create(any())).thenReturn(result);

        CreateSimCardRequestDTO request = new CreateSimCardRequestDTO("8901000000000000001", "351910000001", "268010000000001");

        ResponseEntity<ApiResponseDTO<SimCardDTO>> response = simCardController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("SIM card created successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().id()).isEqualTo(result.id());
        assertThat(response.getBody().data().createdAt()).isEqualTo(result.createdAt());
        assertThat(response.getBody().data().updatedAt()).isEqualTo(result.updatedAt());
        assertThat(response.getBody().data().iccid()).isEqualTo(result.iccid());
        assertThat(response.getBody().data().msisdn()).isEqualTo(result.msisdn());
        assertThat(response.getBody().data().imsi()).isEqualTo(result.imsi());
    }

    @Test
    @DisplayName("Should update sim card and return ok response")
    void should_update_sim_card_and_return_ok_response() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        SimCardResult result = new SimCardResult(id, createdAt, updatedAt, "8901000000000000001", "351910000002", "268010000000002");
        when(simCardUseCase.update(any())).thenReturn(result);

        UpdateSimCardRequestDTO request = new UpdateSimCardRequestDTO("8901000000000000001", "351910000002", "268010000000002");

        ResponseEntity<ApiResponseDTO<SimCardDTO>> response = simCardController.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("SIM card updated successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().id()).isEqualTo(result.id());
        assertThat(response.getBody().data().createdAt()).isEqualTo(result.createdAt());
        assertThat(response.getBody().data().updatedAt()).isEqualTo(result.updatedAt());
        assertThat(response.getBody().data().iccid()).isEqualTo(result.iccid());
        assertThat(response.getBody().data().msisdn()).isEqualTo("351910000002");
        assertThat(response.getBody().data().imsi()).isEqualTo("268010000000002");
    }

    @Test
    @DisplayName("Should get sim card by id and return ok response")
    void should_get_sim_card_by_iccid_and_return_ok_response() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        SimCardResult result = new SimCardResult(id, createdAt, updatedAt, "8901000000000000001", "351910000002", "268010000000002");
        when(simCardUseCase.getById(any())).thenReturn(result);

        ResponseEntity<ApiResponseDTO<SimCardDTO>> response = simCardController.getById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("SIM card fetched successfully.");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().id()).isEqualTo(result.id());
        assertThat(response.getBody().data().createdAt()).isEqualTo(result.createdAt());
        assertThat(response.getBody().data().updatedAt()).isEqualTo(result.updatedAt());
        assertThat(response.getBody().data().iccid()).isEqualTo(result.iccid());
        assertThat(response.getBody().data().msisdn()).isEqualTo("351910000002");
        assertThat(response.getBody().data().imsi()).isEqualTo("268010000000002");
    }

    @Test
    @DisplayName("Should delete sim card by id and return ok response")
    void should_delete_sim_card_by_iccid_and_return_ok_response() {

        UUID id = UUID.randomUUID();
        ResponseEntity<ApiResponseDTO<Void>> response = simCardController.deleteById(id);

        verify(simCardUseCase).deleteById(any());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("SIM card deleted successfully.");
        assertThat(response.getBody().data()).isNull();
    }

}

