package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetVehiclePageCommand;
import com.github.diogocerqueiralima.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.application.exceptions.VehicleAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.assets.Vehicle;
import com.github.diogocerqueiralima.application.ports.outbound.VehiclePersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleUseCaseImplTest {

    @Mock
    private VehiclePersistence vehiclePersistence;

    @InjectMocks
    private VehicleUseCaseImpl vehicleUseCase;

    @Test
    void shouldCreateVehicleWhenVinAndPlateAreUnique() {

        CreateVehicleCommand command = new CreateVehicleCommand(
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        Instant now = Instant.parse("2026-03-15T12:00:00Z");
        Vehicle savedVehicle = new Vehicle(
                UUID.randomUUID(),
                now,
                now,
                command.vin(),
                command.plate(),
                command.model(),
                command.manufacturer(),
                command.manufacturingDate()
        );

        when(vehiclePersistence.existsByVin(command.vin())).thenReturn(false);
        when(vehiclePersistence.existsByPlate(command.plate())).thenReturn(false);
        when(vehiclePersistence.save(any(Vehicle.class))).thenReturn(savedVehicle);

        VehicleResult result = vehicleUseCase.create(command);

        assertThat(result.id()).isEqualTo(savedVehicle.getId());
        assertThat(result.vin()).isEqualTo(command.vin());
        assertThat(result.plate()).isEqualTo(command.plate());
        verify(vehiclePersistence).save(any(Vehicle.class));
    }

    @Test
    void shouldFailWhenVinAlreadyExists() {

        CreateVehicleCommand command = new CreateVehicleCommand(
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        when(vehiclePersistence.existsByVin(command.vin())).thenReturn(true);

        assertThatThrownBy(() -> vehicleUseCase.create(command))
                .isInstanceOf(VehicleAlreadyExistsException.class)
                .hasMessage("A vehicle with the provided VIN already exists.");

        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldFailWhenPlateAlreadyExists() {

        CreateVehicleCommand command = new CreateVehicleCommand(
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        when(vehiclePersistence.existsByVin(command.vin())).thenReturn(false);
        when(vehiclePersistence.existsByPlate(command.plate())).thenReturn(true);

        assertThatThrownBy(() -> vehicleUseCase.create(command))
                .isInstanceOf(VehicleAlreadyExistsException.class)
                .hasMessage("A vehicle with the provided plate already exists.");

        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldUpdateVehicleWhenVehicleExistsAndVinAndPlateAreUnique() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-15T12:00:00Z");

        Vehicle existingVehicle = new Vehicle(
                id,
                createdAt,
                createdAt,
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        UpdateVehicleCommand command = new UpdateVehicleCommand(
                id,
                "1HGCM82633A123456",
                "BB-11-BB",
                "Model Y",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        Vehicle updatedVehicle = new Vehicle(
                id,
                createdAt,
                Instant.parse("2026-03-16T12:00:00Z"),
                command.vin(),
                command.plate(),
                command.model(),
                command.manufacturer(),
                command.manufacturingDate()
        );

        when(vehiclePersistence.findById(id)).thenReturn(java.util.Optional.of(existingVehicle));
        when(vehiclePersistence.existsByPlate(command.plate())).thenReturn(false);
        when(vehiclePersistence.save(any(Vehicle.class))).thenReturn(updatedVehicle);

        VehicleResult result = vehicleUseCase.update(command);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.plate()).isEqualTo("BB-11-BB");
        assertThat(result.model()).isEqualTo("Model Y");
        verify(vehiclePersistence).save(any(Vehicle.class));
    }

    @Test
    void shouldFailUpdatingWhenVehicleDoesNotExist() {

        UUID id = UUID.randomUUID();
        UpdateVehicleCommand command = new UpdateVehicleCommand(
                id,
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        when(vehiclePersistence.findById(id)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> vehicleUseCase.update(command))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found for id: " + id);

        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldFailUpdatingWhenVinAlreadyExistsInAnotherVehicle() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-15T12:00:00Z");
        Vehicle existingVehicle = new Vehicle(
                id,
                createdAt,
                createdAt,
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        UpdateVehicleCommand command = new UpdateVehicleCommand(
                id,
                "1HGCM82633A999999",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        when(vehiclePersistence.findById(id)).thenReturn(java.util.Optional.of(existingVehicle));
        when(vehiclePersistence.existsByVin(command.vin())).thenReturn(true);

        assertThatThrownBy(() -> vehicleUseCase.update(command))
                .isInstanceOf(VehicleAlreadyExistsException.class)
                .hasMessage("A vehicle with the provided VIN already exists.");

        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldFailUpdatingWhenPlateAlreadyExistsInAnotherVehicle() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-15T12:00:00Z");
        Vehicle existingVehicle = new Vehicle(
                id,
                createdAt,
                createdAt,
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        UpdateVehicleCommand command = new UpdateVehicleCommand(
                id,
                "1HGCM82633A123456",
                "CC-22-CC",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        when(vehiclePersistence.findById(id)).thenReturn(java.util.Optional.of(existingVehicle));
        when(vehiclePersistence.existsByPlate(command.plate())).thenReturn(true);

        assertThatThrownBy(() -> vehicleUseCase.update(command))
                .isInstanceOf(VehicleAlreadyExistsException.class)
                .hasMessage("A vehicle with the provided plate already exists.");

        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldGetVehicleByIdWhenVehicleExists() {

        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        Vehicle vehicle = new Vehicle(
                id,
                now,
                now,
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        GetVehicleByIdCommand command = new GetVehicleByIdCommand(id);

        when(vehiclePersistence.findById(id)).thenReturn(java.util.Optional.of(vehicle));

        VehicleResult result = vehicleUseCase.getById(command);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.vin()).isEqualTo(vehicle.getVin());
        assertThat(result.plate()).isEqualTo(vehicle.getPlate());
    }

    @Test
    void shouldFailGettingVehicleByIdWhenVehicleDoesNotExist() {

        UUID id = UUID.randomUUID();
        GetVehicleByIdCommand command = new GetVehicleByIdCommand(id);

        when(vehiclePersistence.findById(id)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> vehicleUseCase.getById(command))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found for id: " + id);
    }

    @Test
    void shouldGetVehiclePageWhenVehiclesExist() {

        int pageNumber = 1;
        int pageSize = 10;

        Vehicle vehicle = new Vehicle(
                UUID.randomUUID(),
                Instant.parse("2026-03-15T12:00:00Z"),
                Instant.parse("2026-03-15T12:00:00Z"),
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        Page<Vehicle> vehiclePage = new PageImpl<>(
                List.of(vehicle),
                PageRequest.of(0, pageSize),
                1
        );

        GetVehiclePageCommand command = new GetVehiclePageCommand(pageNumber, pageSize);

        when(vehiclePersistence.getPage(pageNumber, pageSize)).thenReturn(vehiclePage);

        PageResult<VehicleResult> result = vehicleUseCase.getPage(command);

        assertThat(result.pageNumber()).isEqualTo(pageNumber);
        assertThat(result.pageSize()).isEqualTo(pageSize);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().getFirst().id()).isEqualTo(vehicle.getId());

        verify(vehiclePersistence).getPage(pageNumber, pageSize);
    }

}

