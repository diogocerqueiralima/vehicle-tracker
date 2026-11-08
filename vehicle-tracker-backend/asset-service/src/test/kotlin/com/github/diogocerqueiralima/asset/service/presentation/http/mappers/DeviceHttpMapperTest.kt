package com.github.diogocerqueiralima.asset.service.presentation.http.mappers

import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateOrUpdateDeviceRequestDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class DeviceHttpMapperTest {

    @Test
    @DisplayName("Should map create-or-update request to command using the path id")
    fun should_map_create_or_update_request_to_command() {
        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val request = CreateOrUpdateDeviceRequestDTO(
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345",
            ownerId
        )

        val command = DeviceHttpMapper.toCommand(id, request)

        assertEquals(id, command.id)
        assertEquals(request.serialNumber, command.serialNumber)
        assertEquals(request.imei, command.imei)
        assertEquals(ownerId, command.ownerId)
    }

    @Test
    @DisplayName("Should map result to dto")
    fun should_map_result_to_dto() {

        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val now = Instant.parse("2026-03-15T12:00:00Z")
        val result = DeviceResult(
            id,
            ownerId,
            now,
            now,
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val dto = DeviceHttpMapper.toDTO(result)

        assertEquals(result.id, dto.id)
        assertEquals(ownerId, result.ownerId)
        assertEquals(result.serialNumber, dto.serialNumber)
        assertEquals(result.manufacturer, dto.manufacturer)
    }

    @Test
    @DisplayName("Should map path id to get by id command")
    fun should_map_path_id_to_get_by_id_command() {

        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val isAdmin = true

        val command = DeviceHttpMapper.toGetByIdCommand(id, userId, isAdmin)

        assertEquals(id, command.id)
        assertEquals(userId, command.userId)
        assertTrue(command.isAdmin)
    }

    @Test
    @DisplayName("Should map query params to get pageNumber command")
    fun should_map_query_params_to_get_page_command() {
        val userId = UUID.randomUUID()

        val command = DeviceHttpMapper.toGetPageCommand(2, 15, userId)

        assertEquals(2, command.pageNumber)
        assertEquals(15, command.pageSize)
        assertEquals(userId, command.userId)
    }

    @Test
    @DisplayName("Should map pageNumber result to pageNumber dto")
    fun should_map_page_result_to_page_dto() {

        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val now = Instant.parse("2026-03-15T12:00:00Z")
        val deviceResult = DeviceResult(
            id,
            ownerId,
            now,
            now,
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val result = PageResult(1, 10, 1, 1, listOf(deviceResult))
        val dto = DeviceHttpMapper.toPageDTO(result)

        assertEquals(1, dto.pageNumber)
        assertEquals(10, dto.pageSize)
        assertEquals(1L, dto.totalElements)
        assertEquals(1, dto.data.size)
        assertEquals(id, dto.data.first().id)
    }

}
