package com.github.diogocerqueiralima.presentation.mappers;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetVehiclePageCommand;
import com.github.diogocerqueiralima.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.presentation.dto.CreateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.PageDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.VehicleDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehiclePresentationMapperTest {

    @Test
    @DisplayName("Should map create request to command")
    void should_map_create_request_to_command() {
        UUID userId = UUID.randomUUID();
        CreateVehicleRequestDTO request = new CreateVehicleRequestDTO(
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        CreateVehicleCommand command = VehiclePresentationMapper.toCreateCommand(request, userId);

        assertThat(command.vin()).isEqualTo(request.vin());
        assertThat(command.plate()).isEqualTo(request.plate());
        assertThat(command.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should map update request to command")
    void should_map_update_request_to_command() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UpdateVehicleRequestDTO request = new UpdateVehicleRequestDTO(
                "1HGCM82633A123456",
                "BB-11-BB",
                "Model Y",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        UpdateVehicleCommand command = VehiclePresentationMapper.toUpdateCommand(id, request, userId);

        assertThat(command.id()).isEqualTo(id);
        assertThat(command.plate()).isEqualTo(request.plate());
        assertThat(command.model()).isEqualTo(request.model());
        assertThat(command.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should map result to dto")
    void should_map_result_to_dto() {
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

        VehicleDTO dto = VehiclePresentationMapper.toDTO(result);

        assertThat(dto.id()).isEqualTo(result.id());
        assertThat(dto.vin()).isEqualTo(result.vin());
        assertThat(dto.manufacturer()).isEqualTo(result.manufacturer());
    }

    @Test
    @DisplayName("Should map path id to get by id command")
    void should_map_path_id_to_get_by_id_command() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        GetVehicleByIdCommand command = VehiclePresentationMapper.toGetByIdCommand(id, userId);

        assertThat(command.id()).isEqualTo(id);
        assertThat(command.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should map query params to get page command")
    void should_map_query_params_to_get_page_command() {
        UUID userId = UUID.randomUUID();

        GetVehiclePageCommand command = VehiclePresentationMapper.toGetPageCommand(2, 15, userId);

        assertThat(command.pageNumber()).isEqualTo(2);
        assertThat(command.pageSize()).isEqualTo(15);
        assertThat(command.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should map page result to page dto")
    void should_map_page_result_to_page_dto() {

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

        PageResult<VehicleResult> result = new PageResult<>(1, 10, 1, 1, List.of(vehicleResult));
        PageDTO<VehicleDTO> dto = VehiclePresentationMapper.toPageDTO(result);

        assertThat(dto.pageNumber()).isEqualTo(1);
        assertThat(dto.pageSize()).isEqualTo(10);
        assertThat(dto.totalElements()).isEqualTo(1);
        assertThat(dto.data()).hasSize(1);
        assertThat(dto.data().getFirst().id()).isEqualTo(id);
    }
}

