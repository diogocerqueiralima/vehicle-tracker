package com.github.diogocerqueiralima.asset.service.presentation.http.controllers

import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.DeviceUseCase
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateOrUpdateDeviceRequestDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class DeviceControllerTest {

    @Mock
    private lateinit var deviceUseCase: DeviceUseCase

    @InjectMocks
    private lateinit var deviceController: DeviceController

    @Test
    @DisplayName("Should create or update device and return ok response")
    fun should_create_or_update_device_and_return_ok_response() {

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

        whenever(deviceUseCase.createOrUpdate(any())).thenReturn(result)

        val request = CreateOrUpdateDeviceRequestDTO(
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345",
            ownerId
        )

        val response = deviceController.createOrUpdate(id, request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        val body = response.body!!
        assertEquals("Device saved successfully.", body.message)
        assertNotNull(body.data)
        assertEquals(id, body.data.id)
        assertEquals("SN-001", body.data.serialNumber)
        assertEquals("123456789012345", body.data.imei)
    }

    @Test
    @DisplayName("Should map response data from use case result")
    fun should_map_response_data_from_use_case_result() {

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

        whenever(deviceUseCase.createOrUpdate(any())).thenReturn(result)

        val request = CreateOrUpdateDeviceRequestDTO(
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345",
            ownerId
        )

        val response = deviceController.createOrUpdate(id, request)

        assertNotNull(response.body)
        val body = response.body!!
        assertNotNull(body.data)
        assertEquals("TK-1000", body.data.model)
        assertEquals("Teltonika", body.data.manufacturer)
    }

    @Test
    @DisplayName("Should get device by id and return ok response")
    fun should_get_device_by_id_and_return_ok_response() {

        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val now = Instant.parse("2026-03-15T12:00:00Z")

        val result = DeviceResult(
            id,
            userId,
            now,
            now,
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        whenever(deviceUseCase.getById(any())).thenReturn(result)

        val response = deviceController.getById(id, userId.toString(), null)

        val commandCaptor = argumentCaptor<GetDeviceByIdCommand>()
        verify(deviceUseCase).getById(commandCaptor.capture())
        val capturedCommand = commandCaptor.firstValue

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        val body = response.body!!
        assertEquals("Device fetched successfully.", body.message)
        assertNotNull(body.data)
        assertEquals(id, body.data.id)
        assertEquals("SN-001", body.data.serialNumber)
        assertEquals(id, capturedCommand.id)
        assertEquals(userId, capturedCommand.userId)
        assertFalse(capturedCommand.isAdmin)
    }

    @Test
    @DisplayName("Should get device pageNumber and return ok response")
    fun should_get_device_page_and_return_ok_response() {
        val userId = UUID.randomUUID()

        val id = UUID.randomUUID()
        val now = Instant.parse("2026-03-15T12:00:00Z")
        val deviceResult = DeviceResult(
            id,
            UUID(0, 0),
            now,
            now,
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        whenever(deviceUseCase.getPage(any())).thenReturn(PageResult(1, 10, 1, 1, listOf(deviceResult)))

        val response = deviceController.getPage(userId.toString(), 1, 10)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        val body = response.body!!
        assertEquals("Devices fetched successfully.", body.message)
        assertNotNull(body.data)
        assertEquals(1, body.data.pageNumber)
        assertEquals(10, body.data.pageSize)
        assertEquals(1, body.data.data.size)
        assertEquals(id, body.data.data.first().id)
    }

}
