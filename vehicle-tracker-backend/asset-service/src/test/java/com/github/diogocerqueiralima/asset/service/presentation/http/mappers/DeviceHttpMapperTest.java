package com.github.diogocerqueiralima.asset.service.presentation.http.mappers;

import com.github.diogocerqueiralima.asset.service.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateDeviceCommand;
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateDeviceRequestDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.DeviceDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.PageDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UpdateDeviceRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeviceHttpMapperTest {

    @Test
    @DisplayName("Should map create request to command")
    void should_map_create_request_to_command() {
        UUID ownerId = UUID.randomUUID();
        CreateDeviceRequestDTO request = new CreateDeviceRequestDTO(
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345",
                ownerId
        );

        CreateDeviceCommand command = DeviceHttpMapper.toCreateCommand(request);

        assertEquals(request.serialNumber(), command.serialNumber());
        assertEquals(request.imei(), command.imei());
        assertEquals(ownerId, command.ownerId());
    }

    @Test
    @DisplayName("Should map update request to command")
    void should_map_update_request_to_command() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UpdateDeviceRequestDTO request = new UpdateDeviceRequestDTO(
                "SN-002",
                "TK-1100",
                "Teltonika",
                "223456789012345",
                ownerId
        );

        UpdateDeviceCommand command = DeviceHttpMapper.toUpdateCommand(id, request);

        assertEquals(id, command.id());
        assertEquals(request.serialNumber(), command.serialNumber());
        assertEquals(request.model(), command.model());
        assertEquals(ownerId, command.ownerId());
    }

    @Test
    @DisplayName("Should map result to dto")
    void should_map_result_to_dto() {

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

        DeviceDTO dto = DeviceHttpMapper.toDTO(result);

        assertEquals(result.id(), dto.id());
        assertEquals(ownerId, result.ownerId());
        assertEquals(result.serialNumber(), dto.serialNumber());
        assertEquals(result.manufacturer(), dto.manufacturer());
    }

    @Test
    @DisplayName("Should map path id to get by id command")
    void should_map_path_id_to_get_by_id_command() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        boolean isAdmin = true;

        GetDeviceByIdCommand command = DeviceHttpMapper.toGetByIdCommand(id, userId, isAdmin);

        assertEquals(id, command.id());
        assertEquals(userId, command.userId());
        assertTrue(command.isAdmin());
    }

    @Test
    @DisplayName("Should map query params to get pageNumber command")
    void should_map_query_params_to_get_page_command() {
        UUID userId = UUID.randomUUID();

        GetDevicePageCommand command = DeviceHttpMapper.toGetPageCommand(2, 15, userId);

        assertEquals(2, command.pageNumber());
        assertEquals(15, command.pageSize());
        assertEquals(userId, command.userId());
    }

    @Test
    @DisplayName("Should map pageNumber result to pageNumber dto")
    void should_map_page_result_to_page_dto() {

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");
        DeviceResult deviceResult = new DeviceResult(
                id,
                ownerId,
                now,
                now,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        PageResult<DeviceResult> result = new PageResult<>(1, 10, 1, 1, List.of(deviceResult));
        PageDTO<DeviceDTO> dto = DeviceHttpMapper.toPageDTO(result);

        assertEquals(1, dto.pageNumber());
        assertEquals(10, dto.pageSize());
        assertEquals(1, dto.totalElements());
        assertEquals(1, dto.data().size());
        assertEquals(id, dto.data().getFirst().id());
    }

}