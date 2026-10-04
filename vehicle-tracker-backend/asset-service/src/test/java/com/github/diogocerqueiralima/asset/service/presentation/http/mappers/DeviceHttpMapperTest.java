package com.github.diogocerqueiralima.asset.service.presentation.http.mappers;

import com.github.diogocerqueiralima.api.common.dto.PageDTO;
import com.github.diogocerqueiralima.asset.service.application.commands.CreateOrUpdateDeviceCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateOrUpdateDeviceRequestDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.DeviceDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeviceHttpMapperTest {

    @Test
    @DisplayName("Should map create-or-update request to command using the path id")
    void should_map_create_or_update_request_to_command() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        CreateOrUpdateDeviceRequestDTO request = new CreateOrUpdateDeviceRequestDTO(
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345",
                ownerId
        );

        CreateOrUpdateDeviceCommand command = DeviceHttpMapper.INSTANCE.toCommand(id, request);

        assertEquals(id, command.getId());
        assertEquals(request.getSerialNumber(), command.getSerialNumber());
        assertEquals(request.getImei(), command.getImei());
        assertEquals(ownerId, command.getOwnerId());
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

        DeviceDTO dto = DeviceHttpMapper.INSTANCE.toDTO(result);

        assertEquals(result.getId(), dto.getId());
        assertEquals(ownerId, result.getOwnerId());
        assertEquals(result.getSerialNumber(), dto.getSerialNumber());
        assertEquals(result.getManufacturer(), dto.getManufacturer());
    }

    @Test
    @DisplayName("Should map path id to get by id command")
    void should_map_path_id_to_get_by_id_command() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        boolean isAdmin = true;

        GetDeviceByIdCommand command = DeviceHttpMapper.INSTANCE.toGetByIdCommand(id, userId, isAdmin);

        assertEquals(id, command.getId());
        assertEquals(userId, command.getUserId());
        assertTrue(command.isAdmin());
    }

    @Test
    @DisplayName("Should map query params to get pageNumber command")
    void should_map_query_params_to_get_page_command() {
        UUID userId = UUID.randomUUID();

        GetDevicePageCommand command = DeviceHttpMapper.INSTANCE.toGetPageCommand(2, 15, userId);

        assertEquals(2, command.getPageNumber());
        assertEquals(15, command.getPageSize());
        assertEquals(userId, command.getUserId());
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
        PageDTO<DeviceDTO> dto = DeviceHttpMapper.INSTANCE.toPageDTO(result);

        assertEquals(1, dto.pageNumber());
        assertEquals(10, dto.pageSize());
        assertEquals(1, dto.totalElements());
        assertEquals(1, dto.data().size());
        assertEquals(id, dto.data().getFirst().getId());
    }

}