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
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehiclePresentationMapperTest {

    @Test
    void shouldMapCreateRequestToCommand() {
        CreateVehicleRequestDTO request = new CreateVehicleRequestDTO(
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        CreateVehicleCommand command = VehiclePresentationMapper.toCreateCommand(request);

        assertThat(command.vin()).isEqualTo(request.vin());
        assertThat(command.plate()).isEqualTo(request.plate());
    }

    @Test
    void shouldMapUpdateRequestToCommand() {
        UUID id = UUID.randomUUID();
        UpdateVehicleRequestDTO request = new UpdateVehicleRequestDTO(
                "1HGCM82633A123456",
                "BB-11-BB",
                "Model Y",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        UpdateVehicleCommand command = VehiclePresentationMapper.toUpdateCommand(id, request);

        assertThat(command.id()).isEqualTo(id);
        assertThat(command.plate()).isEqualTo(request.plate());
        assertThat(command.model()).isEqualTo(request.model());
    }

    @Test
    void shouldMapResultToDto() {
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
    void shouldMapPathIdToGetByIdCommand() {
        UUID id = UUID.randomUUID();

        GetVehicleByIdCommand command = VehiclePresentationMapper.toGetByIdCommand(id);

        assertThat(command.id()).isEqualTo(id);
    }

    @Test
    void shouldMapQueryParamsToGetPageCommand() {

        GetVehiclePageCommand command = VehiclePresentationMapper.toGetPageCommand(2, 15);

        assertThat(command.pageNumber()).isEqualTo(2);
        assertThat(command.pageSize()).isEqualTo(15);
    }

    @Test
    void shouldMapPageResultToPageDTO() {

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

