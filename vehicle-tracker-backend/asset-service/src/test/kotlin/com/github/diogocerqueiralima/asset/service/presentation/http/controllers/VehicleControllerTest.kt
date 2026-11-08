package com.github.diogocerqueiralima.asset.service.presentation.http.controllers

import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.application.results.VehicleResult
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.VehicleUseCase
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateVehicleRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UpdateVehicleRequestDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class VehicleControllerTest {

    @Mock
    private lateinit var vehicleUseCase: VehicleUseCase

    @InjectMocks
    private lateinit var vehicleController: VehicleController

    @Test
    @DisplayName("Should create vehicle and return created response")
    fun should_create_vehicle_and_return_created_response() {
        val userId = UUID.randomUUID()

        val id = UUID.randomUUID()
        val now = Instant.parse("2026-03-15T12:00:00Z")

        val result = VehicleResult(
            id,
            now,
            now,
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        whenever(vehicleUseCase.create(any())).thenReturn(result)

        val request = CreateVehicleRequestDTO(
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        val response = vehicleController.create(userId.toString(), request)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("Vehicle created successfully.")
        assertThat(body.data).isNotNull()
        assertThat(body.data.id).isEqualTo(id)
        assertThat(body.data.vin).isEqualTo("1HGCM82633A123456")
        assertThat(body.data.plate).isEqualTo("AA-00-AA")
    }

    @Test
    @DisplayName("Should map response data from use case result")
    fun should_map_response_data_from_use_case_result() {
        val userId = UUID.randomUUID()

        val id = UUID.randomUUID()
        val now = Instant.parse("2026-03-15T12:00:00Z")

        val result = VehicleResult(
            id,
            now,
            now,
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        whenever(vehicleUseCase.create(any())).thenReturn(result)

        val request = CreateVehicleRequestDTO(
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        val response = vehicleController.create(userId.toString(), request)

        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.data).isNotNull()
        assertThat(body.data.model).isEqualTo("Model 3")
        assertThat(body.data.manufacturer).isEqualTo("Tesla")
        assertThat(body.data.manufacturingDate).isEqualTo(LocalDate.of(2024, 1, 15))
    }

    @Test
    @DisplayName("Should update vehicle and return ok response")
    fun should_update_vehicle_and_return_ok_response() {
        val userId = UUID.randomUUID()

        val id = UUID.randomUUID()
        val createdAt = Instant.parse("2026-03-15T12:00:00Z")
        val updatedAt = Instant.parse("2026-03-16T12:00:00Z")

        val result = VehicleResult(
            id,
            createdAt,
            updatedAt,
            "1HGCM82633A123456",
            "BB-11-BB",
            "Model Y",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        whenever(vehicleUseCase.update(any())).thenReturn(result)

        val request = UpdateVehicleRequestDTO(
            "1HGCM82633A123456",
            "BB-11-BB",
            "Model Y",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        val response = vehicleController.update(id, userId.toString(), request)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("Vehicle updated successfully.")
        assertThat(body.data).isNotNull()
        assertThat(body.data.id).isEqualTo(id)
        assertThat(body.data.plate).isEqualTo("BB-11-BB")
        assertThat(body.data.model).isEqualTo("Model Y")
    }

    @Test
    @DisplayName("Should get vehicle by id and return ok response")
    fun should_get_vehicle_by_id_and_return_ok_response() {
        val userId = UUID.randomUUID()

        val id = UUID.randomUUID()
        val now = Instant.parse("2026-03-15T12:00:00Z")

        val result = VehicleResult(
            id,
            now,
            now,
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        whenever(vehicleUseCase.getById(any())).thenReturn(result)

        val response = vehicleController.getById(id, userId.toString())

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("Vehicle fetched successfully.")
        assertThat(body.data).isNotNull()
        assertThat(body.data.id).isEqualTo(id)
        assertThat(body.data.vin).isEqualTo("1HGCM82633A123456")
    }

    @Test
    @DisplayName("Should get vehicle pageNumber and return ok response")
    fun should_get_vehicle_page_and_return_ok_response() {
        val userId = UUID.randomUUID()

        val id = UUID.randomUUID()
        val now = Instant.parse("2026-03-15T12:00:00Z")
        val vehicleResult = VehicleResult(
            id,
            now,
            now,
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        whenever(vehicleUseCase.getPage(any())).thenReturn(PageResult(1, 10, 1, 1, listOf(vehicleResult)))

        val response = vehicleController.getPage(userId.toString(), 1, 10)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("Vehicles fetched successfully.")
        assertThat(body.data).isNotNull()
        assertThat(body.data.pageNumber).isEqualTo(1)
        assertThat(body.data.pageSize).isEqualTo(10)
        assertThat(body.data.data).hasSize(1)
        assertThat(body.data.data.first().id).isEqualTo(id)
    }

}
