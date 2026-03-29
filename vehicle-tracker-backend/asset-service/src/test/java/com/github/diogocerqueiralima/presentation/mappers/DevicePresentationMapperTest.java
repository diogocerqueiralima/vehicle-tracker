package com.github.diogocerqueiralima.presentation.mappers;

import com.github.diogocerqueiralima.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.application.commands.UpdateDeviceCommand;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.presentation.dto.CreateDeviceRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.DeviceDTO;
import com.github.diogocerqueiralima.presentation.dto.PageDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateDeviceRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DevicePresentationMapperTest {

    @Test
    @DisplayName("Should map create request to command")
    void should_map_create_request_to_command() {
        CreateDeviceRequestDTO request = new CreateDeviceRequestDTO(
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        CreateDeviceCommand command = DevicePresentationMapper.toCreateCommand(request);

        assertThat(command.serialNumber()).isEqualTo(request.serialNumber());
        assertThat(command.imei()).isEqualTo(request.imei());
    }

    @Test
    @DisplayName("Should map update request to command")
    void should_map_update_request_to_command() {
        UUID id = UUID.randomUUID();
        UpdateDeviceRequestDTO request = new UpdateDeviceRequestDTO(
                "SN-002",
                "TK-1100",
                "Teltonika",
                "223456789012345"
        );

        UpdateDeviceCommand command = DevicePresentationMapper.toUpdateCommand(id, request);

        assertThat(command.id()).isEqualTo(id);
        assertThat(command.serialNumber()).isEqualTo(request.serialNumber());
        assertThat(command.model()).isEqualTo(request.model());
    }

    @Test
    @DisplayName("Should map result to dto")
    void should_map_result_to_dto() {
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

        DeviceDTO dto = DevicePresentationMapper.toDTO(result);

        assertThat(dto.id()).isEqualTo(result.id());
        assertThat(dto.serialNumber()).isEqualTo(result.serialNumber());
        assertThat(dto.manufacturer()).isEqualTo(result.manufacturer());
    }

    @Test
    @DisplayName("Should map path id to get by id command")
    void should_map_path_id_to_get_by_id_command() {
        UUID id = UUID.randomUUID();

        GetDeviceByIdCommand command = DevicePresentationMapper.toGetByIdCommand(id);

        assertThat(command.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("Should map query params to get page command")
    void should_map_query_params_to_get_page_command() {

        GetDevicePageCommand command = DevicePresentationMapper.toGetPageCommand(2, 15);

        assertThat(command.pageNumber()).isEqualTo(2);
        assertThat(command.pageSize()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should map page result to page dto")
    void should_map_page_result_to_page_dto() {

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

        PageResult<DeviceResult> result = new PageResult<>(1, 10, 1, 1, List.of(deviceResult));
        PageDTO<DeviceDTO> dto = DevicePresentationMapper.toPageDTO(result);

        assertThat(dto.pageNumber()).isEqualTo(1);
        assertThat(dto.pageSize()).isEqualTo(10);
        assertThat(dto.totalElements()).isEqualTo(1);
        assertThat(dto.data()).hasSize(1);
        assertThat(dto.data().getFirst().id()).isEqualTo(id);
    }

}

