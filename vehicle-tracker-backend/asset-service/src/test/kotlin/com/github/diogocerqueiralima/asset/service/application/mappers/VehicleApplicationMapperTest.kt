package com.github.diogocerqueiralima.asset.service.application.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.CreateVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateVehicleCommand
import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class VehicleApplicationMapperTest {

    @Test
    @DisplayName("Should map create command to domain")
    fun should_map_create_command_to_domain() {
        val userId = UUID.randomUUID()
        val command = CreateVehicleCommand(
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15),
            userId
        )

        val now = Instant.parse("2026-03-15T12:00:00Z")
        val vehicle = VehicleApplicationMapper.toDomain(command, now)

        assertThat(vehicle.id).isNotNull()
        assertThat(vehicle.createdAt).isEqualTo(now)
        assertThat(vehicle.updatedAt).isEqualTo(now)
        assertThat(vehicle.vin).isEqualTo(command.vin)
        assertThat(vehicle.ownerId).isEqualTo(userId)
    }

    @Test
    @DisplayName("Should map update command to domain using existing vehicle identity")
    fun should_map_update_command_to_domain_using_existing_vehicle_identity() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val createdAt = Instant.parse("2026-03-10T12:00:00Z")
        val existingVehicle = Vehicle(
            id,
            userId,
            createdAt,
            createdAt,
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        val command = UpdateVehicleCommand(
            id,
            "1HGCM82633A123456",
            "BB-11-BB",
            "Model Y",
            "Tesla",
            LocalDate.of(2024, 1, 15),
            userId
        )

        val updatedAt = Instant.parse("2026-03-20T10:00:00Z")
        val mapped = VehicleApplicationMapper.toDomain(command, existingVehicle, updatedAt)

        assertThat(mapped.id).isEqualTo(id)
        assertThat(mapped.createdAt).isEqualTo(createdAt)
        assertThat(mapped.updatedAt).isEqualTo(updatedAt)
        assertThat(mapped.plate).isEqualTo("BB-11-BB")
        assertThat(mapped.ownerId).isEqualTo(userId)
    }

    @Test
    @DisplayName("Should map domain to result")
    fun should_map_domain_to_result() {
        val id = UUID.randomUUID()
        val now = Instant.parse("2026-03-15T12:00:00Z")
        val vehicle = Vehicle(
            id,
            now,
            now,
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        val result = VehicleApplicationMapper.toResult(vehicle)

        assertThat(result.id).isEqualTo(id)
        assertThat(result.vin).isEqualTo(vehicle.vin)
        assertThat(result.plate).isEqualTo(vehicle.plate)
    }

}
