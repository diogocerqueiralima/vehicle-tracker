package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.exceptions.VehicleAlreadyExistsException;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.model.assets.Vehicle;
import com.github.diogocerqueiralima.domain.ports.outbound.VehiclePersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
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
                "ABCD1234",
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
                "ABCD1234",
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
                "ABCD1234",
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

}

