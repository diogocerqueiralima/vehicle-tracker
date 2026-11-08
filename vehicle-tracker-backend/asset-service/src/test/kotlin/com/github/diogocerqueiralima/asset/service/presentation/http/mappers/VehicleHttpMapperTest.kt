package com.github.diogocerqueiralima.asset.service.presentation.http.mappers

import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.application.results.VehicleResult
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateVehicleRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UpdateVehicleRequestDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class VehicleHttpMapperTest {

    @Test
    @DisplayName("Should map create request to command")
    fun should_map_create_request_to_command() {
        val userId = UUID.randomUUID()
        val request = CreateVehicleRequestDTO(
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        val command = VehicleHttpMapper.toCreateCommand(request, userId)

        assertThat(command.vin).isEqualTo(request.vin)
        assertThat(command.plate).isEqualTo(request.plate)
        assertThat(command.userId).isEqualTo(userId)
    }

    @Test
    @DisplayName("Should map update request to command")
    fun should_map_update_request_to_command() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val request = UpdateVehicleRequestDTO(
            "1HGCM82633A123456",
            "BB-11-BB",
            "Model Y",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        val command = VehicleHttpMapper.toUpdateCommand(id, request, userId)

        assertThat(command.id).isEqualTo(id)
        assertThat(command.plate).isEqualTo(request.plate)
        assertThat(command.model).isEqualTo(request.model)
        assertThat(command.userId).isEqualTo(userId)
    }

    @Test
    @DisplayName("Should map result to dto")
    fun should_map_result_to_dto() {
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

        val dto = VehicleHttpMapper.toDTO(result)

        assertThat(dto.id).isEqualTo(result.id)
        assertThat(dto.vin).isEqualTo(result.vin)
        assertThat(dto.manufacturer).isEqualTo(result.manufacturer)
    }

    @Test
    @DisplayName("Should map path id to get by id command")
    fun should_map_path_id_to_get_by_id_command() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val command = VehicleHttpMapper.toGetByIdCommand(id, userId)

        assertThat(command.id).isEqualTo(id)
        assertThat(command.userId).isEqualTo(userId)
    }

    @Test
    @DisplayName("Should map query params to get pageNumber command")
    fun should_map_query_params_to_get_page_command() {
        val userId = UUID.randomUUID()

        val command = VehicleHttpMapper.toGetPageCommand(2, 15, userId)

        assertThat(command.pageNumber).isEqualTo(2)
        assertThat(command.pageSize).isEqualTo(15)
        assertThat(command.userId).isEqualTo(userId)
    }

    @Test
    @DisplayName("Should map pageNumber result to pageNumber dto")
    fun should_map_page_result_to_page_dto() {

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

        val result = PageResult(1, 10, 1, 1, listOf(vehicleResult))
        val dto = VehicleHttpMapper.toPageDTO(result)

        assertThat(dto.pageNumber).isEqualTo(1)
        assertThat(dto.pageSize).isEqualTo(10)
        assertThat(dto.totalElements).isEqualTo(1L)
        assertThat(dto.data).hasSize(1)
        assertThat(dto.data.first().id).isEqualTo(id)
    }

}
