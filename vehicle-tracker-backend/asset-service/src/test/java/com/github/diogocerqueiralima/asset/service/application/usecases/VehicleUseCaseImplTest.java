package com.github.diogocerqueiralima.asset.service.application.usecases;

import com.github.diogocerqueiralima.error.common.exceptions.ConflictException;
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException;
import com.github.diogocerqueiralima.asset.service.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehiclePageCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import com.github.diogocerqueiralima.asset.service.application.results.VehicleResult;
import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle;
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehiclePersistence;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Should create vehicle when VIN and plate are unique")
    void should_create_vehicle_when_vin_and_plate_are_unique() {
        UUID userId = UUID.randomUUID();

        CreateVehicleCommand command = new CreateVehicleCommand(
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15),
                userId
        );

        Instant now = Instant.parse("2026-03-15T12:00:00Z");
        Vehicle savedVehicle = new Vehicle(
                UUID.randomUUID(),
                now,
                now,
                command.getVin(),
                command.getPlate(),
                command.getModel(),
                command.getManufacturer(),
                command.getManufacturingDate()
        );

        when(vehiclePersistence.save(any(Vehicle.class))).thenReturn(savedVehicle);

        VehicleResult result = vehicleUseCase.create(command);

        assertThat(result.getId()).isEqualTo(savedVehicle.getId());
        assertThat(result.getVin()).isEqualTo(command.getVin());
        assertThat(result.getPlate()).isEqualTo(command.getPlate());
        verify(vehiclePersistence).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should fail creating when VIN or plate already exists")
    void should_fail_creating_when_vin_or_plate_already_exists() {
        UUID userId = UUID.randomUUID();

        CreateVehicleCommand command = new CreateVehicleCommand(
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15),
                userId
        );

        when(vehiclePersistence.save(any(Vehicle.class))).thenThrow(new ConflictException("A vehicle with the provided VIN or plate already exists."));

        assertThatThrownBy(() -> vehicleUseCase.create(command))
                .isInstanceOf(ConflictException.class);

        verify(vehiclePersistence).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should update vehicle when vehicle exists and VIN and plate are unique")
    void should_update_vehicle_when_vehicle_exists_and_vin_and_plate_are_unique() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
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
                LocalDate.of(2024, 1, 15),
                userId
        );

        Vehicle updatedVehicle = new Vehicle(
                id,
                createdAt,
                Instant.parse("2026-03-16T12:00:00Z"),
                command.getVin(),
                command.getPlate(),
                command.getModel(),
                command.getManufacturer(),
                command.getManufacturingDate()
        );

        when(vehiclePersistence.findByIdAndOwnerId(id, userId)).thenReturn(existingVehicle);
        when(vehiclePersistence.save(any(Vehicle.class))).thenReturn(updatedVehicle);

        VehicleResult result = vehicleUseCase.update(command);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getPlate()).isEqualTo("BB-11-BB");
        assertThat(result.getModel()).isEqualTo("Model Y");
        verify(vehiclePersistence).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should fail updating when vehicle does not exist")
    void should_fail_updating_when_vehicle_does_not_exist() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UpdateVehicleCommand command = new UpdateVehicleCommand(
                id,
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15),
                userId
        );

        when(vehiclePersistence.findByIdAndOwnerId(id, userId)).thenReturn(null);

        assertThatThrownBy(() -> vehicleUseCase.update(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Vehicle not found for id: " + id);

        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should fail updating when VIN or plate already exists in another vehicle")
    void should_fail_updating_when_vin_or_plate_already_exists_in_another_vehicle() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
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
                "CC-22-CC",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15),
                userId
        );

        when(vehiclePersistence.findByIdAndOwnerId(id, userId)).thenReturn(existingVehicle);
        when(vehiclePersistence.save(any(Vehicle.class))).thenThrow(new ConflictException("A vehicle with the provided VIN or plate already exists."));

        assertThatThrownBy(() -> vehicleUseCase.update(command))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Should get vehicle by id when vehicle exists")
    void should_get_vehicle_by_id_when_vehicle_exists() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
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

        GetVehicleByIdCommand command = new GetVehicleByIdCommand(id, userId);

        when(vehiclePersistence.findByIdAndOwnerId(id, userId)).thenReturn(vehicle);

        VehicleResult result = vehicleUseCase.getById(command);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getVin()).isEqualTo(vehicle.getVin());
        assertThat(result.getPlate()).isEqualTo(vehicle.getPlate());
    }

    @Test
    @DisplayName("Should fail getting vehicle by id when vehicle does not exist")
    void should_fail_getting_vehicle_by_id_when_vehicle_does_not_exist() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GetVehicleByIdCommand command = new GetVehicleByIdCommand(id, userId);

        when(vehiclePersistence.findByIdAndOwnerId(id, userId)).thenReturn(null);

        assertThatThrownBy(() -> vehicleUseCase.getById(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Vehicle not found for id: " + id);
    }

    @Test
    @DisplayName("Should get vehicle pageNumber when vehicles exist")
    void should_get_vehicle_page_when_vehicles_exist() {

        int pageNumber = 1;
        int pageSize = 10;
        UUID userId = UUID.randomUUID();

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

        GetVehiclePageCommand command = new GetVehiclePageCommand(pageNumber, pageSize, userId);

        when(vehiclePersistence.getPageByOwnerId(pageNumber - 1, pageSize, userId)).thenReturn(vehiclePage);

        PageResult<VehicleResult> result = vehicleUseCase.getPage(command);

        assertThat(result.getPageNumber()).isEqualTo(pageNumber);
        assertThat(result.getPageSize()).isEqualTo(pageSize);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst().getId()).isEqualTo(vehicle.getId());

        verify(vehiclePersistence).getPageByOwnerId(pageNumber - 1, pageSize, userId);
    }

}
