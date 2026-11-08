package com.github.diogocerqueiralima.asset.service.application.usecases

import com.github.diogocerqueiralima.asset.service.application.commands.CreateVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehiclePageCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateVehicleCommand
import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehiclePersistence
import com.github.diogocerqueiralima.error.common.exceptions.ConflictException
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class VehicleUseCaseImplTest {

    @Mock
    private lateinit var vehiclePersistence: VehiclePersistence

    @InjectMocks
    private lateinit var vehicleUseCase: VehicleUseCaseImpl

    @Test
    @DisplayName("Should create vehicle when VIN and plate are unique")
    fun should_create_vehicle_when_vin_and_plate_are_unique() {
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
        val savedVehicle = Vehicle(
            UUID.randomUUID(),
            now,
            now,
            command.vin,
            command.plate,
            command.model,
            command.manufacturer,
            command.manufacturingDate
        )

        whenever(vehiclePersistence.save(any())).thenReturn(savedVehicle)

        val result = vehicleUseCase.create(command)

        assertThat(result.id).isEqualTo(savedVehicle.id)
        assertThat(result.vin).isEqualTo(command.vin)
        assertThat(result.plate).isEqualTo(command.plate)
        verify(vehiclePersistence).save(any())
    }

    @Test
    @DisplayName("Should fail creating when VIN or plate already exists")
    fun should_fail_creating_when_vin_or_plate_already_exists() {
        val userId = UUID.randomUUID()

        val command = CreateVehicleCommand(
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15),
            userId
        )

        whenever(vehiclePersistence.save(any())).thenThrow(ConflictException("A vehicle with the provided VIN or plate already exists."))

        assertThatThrownBy { vehicleUseCase.create(command) }
            .isInstanceOf(ConflictException::class.java)

        verify(vehiclePersistence).save(any())
    }

    @Test
    @DisplayName("Should update vehicle when vehicle exists and VIN and plate are unique")
    fun should_update_vehicle_when_vehicle_exists_and_vin_and_plate_are_unique() {

        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val createdAt = Instant.parse("2026-03-15T12:00:00Z")

        val existingVehicle = Vehicle(
            id,
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

        val updatedVehicle = Vehicle(
            id,
            createdAt,
            Instant.parse("2026-03-16T12:00:00Z"),
            command.vin,
            command.plate,
            command.model,
            command.manufacturer,
            command.manufacturingDate
        )

        whenever(vehiclePersistence.findByIdAndOwnerId(id, userId)).thenReturn(existingVehicle)
        whenever(vehiclePersistence.save(any())).thenReturn(updatedVehicle)

        val result = vehicleUseCase.update(command)

        assertThat(result.id).isEqualTo(id)
        assertThat(result.createdAt).isEqualTo(createdAt)
        assertThat(result.plate).isEqualTo("BB-11-BB")
        assertThat(result.model).isEqualTo("Model Y")
        verify(vehiclePersistence).save(any())
    }

    @Test
    @DisplayName("Should fail updating when vehicle does not exist")
    fun should_fail_updating_when_vehicle_does_not_exist() {

        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = UpdateVehicleCommand(
            id,
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15),
            userId
        )

        whenever(vehiclePersistence.findByIdAndOwnerId(id, userId)).thenReturn(null)

        assertThatThrownBy { vehicleUseCase.update(command) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Vehicle not found for id: $id")

        verify(vehiclePersistence, never()).save(any())
    }

    @Test
    @DisplayName("Should fail updating when VIN or plate already exists in another vehicle")
    fun should_fail_updating_when_vin_or_plate_already_exists_in_another_vehicle() {

        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val createdAt = Instant.parse("2026-03-15T12:00:00Z")
        val existingVehicle = Vehicle(
            id,
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
            "1HGCM82633A999999",
            "CC-22-CC",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15),
            userId
        )

        whenever(vehiclePersistence.findByIdAndOwnerId(id, userId)).thenReturn(existingVehicle)
        whenever(vehiclePersistence.save(any())).thenThrow(ConflictException("A vehicle with the provided VIN or plate already exists."))

        assertThatThrownBy { vehicleUseCase.update(command) }
            .isInstanceOf(ConflictException::class.java)
    }

    @Test
    @DisplayName("Should get vehicle by id when vehicle exists")
    fun should_get_vehicle_by_id_when_vehicle_exists() {

        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
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

        val command = GetVehicleByIdCommand(id, userId)

        whenever(vehiclePersistence.findByIdAndOwnerId(id, userId)).thenReturn(vehicle)

        val result = vehicleUseCase.getById(command)

        assertThat(result.id).isEqualTo(id)
        assertThat(result.vin).isEqualTo(vehicle.vin)
        assertThat(result.plate).isEqualTo(vehicle.plate)
    }

    @Test
    @DisplayName("Should fail getting vehicle by id when vehicle does not exist")
    fun should_fail_getting_vehicle_by_id_when_vehicle_does_not_exist() {

        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = GetVehicleByIdCommand(id, userId)

        whenever(vehiclePersistence.findByIdAndOwnerId(id, userId)).thenReturn(null)

        assertThatThrownBy { vehicleUseCase.getById(command) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Vehicle not found for id: $id")
    }

    @Test
    @DisplayName("Should get vehicle pageNumber when vehicles exist")
    fun should_get_vehicle_page_when_vehicles_exist() {

        val pageNumber = 1
        val pageSize = 10
        val userId = UUID.randomUUID()

        val vehicle = Vehicle(
            UUID.randomUUID(),
            Instant.parse("2026-03-15T12:00:00Z"),
            Instant.parse("2026-03-15T12:00:00Z"),
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        val vehiclePage = PageImpl(
            listOf(vehicle),
            PageRequest.of(0, pageSize),
            1
        )

        val command = GetVehiclePageCommand(pageNumber, pageSize, userId)

        whenever(vehiclePersistence.getPageByOwnerId(pageNumber - 1, pageSize, userId)).thenReturn(vehiclePage)

        val result = vehicleUseCase.getPage(command)

        assertThat(result.pageNumber).isEqualTo(pageNumber)
        assertThat(result.pageSize).isEqualTo(pageSize)
        assertThat(result.totalElements).isEqualTo(1L)
        assertThat(result.totalPages).isEqualTo(1)
        assertThat(result.data).hasSize(1)
        assertThat(result.data.first().id).isEqualTo(vehicle.id)

        verify(vehiclePersistence).getPageByOwnerId(pageNumber - 1, pageSize, userId)
    }

}
