package com.github.diogocerqueiralima.application.services;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.exceptions.VehicleAlreadyExistsException;
import com.github.diogocerqueiralima.application.mappers.VehicleMapper;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.model.Vehicle;
import com.github.diogocerqueiralima.domain.ports.outbound.VehicleDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleDataSource vehicleDataSource;

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    @BeforeEach
    public void setup() {
        lenient().when(vehicleMapper.toResult(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle v = invocation.getArgument(0);
            return new VehicleResult(
                    v.getId(),
                    v.getVin(),
                    v.getPlate(),
                    v.getModel(),
                    v.getManufacturer(),
                    v.getYear(),
                    v.getOwnerId()
            );
        });
    }

    @Test
    public void create_vehicle_with_existing_vin_should_throw_exception() {

        CreateVehicleCommand command = new CreateVehicleCommand(
                "1HGCM82633A123456",
                "AB-12-C3",
                "Civic",
                "Honda",
                2020,
                UUID.randomUUID()
        );

        when(vehicleDataSource.existsByVin(command.vin()))
                .thenReturn(true);


        assertThrows(VehicleAlreadyExistsException.class, () -> vehicleService.create(command));
    }

    @Test
    public void create_vehicle_with_existing_plate_should_throw_exception() {

        CreateVehicleCommand command = new CreateVehicleCommand(
                "1HGCM82633A123456",
                "AB-12-C3",
                "Civic",
                "Honda",
                2020,
                UUID.randomUUID()
        );

        when(vehicleDataSource.existsByVin(command.vin()))
                .thenReturn(false);

        when(vehicleDataSource.existsByPlate(command.plate()))
                .thenReturn(true);

        assertThrows(VehicleAlreadyExistsException.class, () -> vehicleService.create(command));
    }

    @Test
    public void create_vehicle_should_succeed() {

        CreateVehicleCommand command = new CreateVehicleCommand(
                "1HGCM82633A123456",
                "AB-12-C3",
                "Civic",
                "Honda",
                2020,
                UUID.randomUUID()
        );

        when(vehicleDataSource.existsByVin(command.vin()))
                .thenReturn(false);

        when(vehicleDataSource.existsByPlate(command.plate()))
                .thenReturn(false);

        Vehicle vehicle = new Vehicle(
                UUID.randomUUID(),
                command.vin(),
                command.plate(),
                command.model(),
                command.manufacturer(),
                command.year(),
                command.ownerId()
        );

        when(vehicleDataSource.save(any()))
                .thenReturn(vehicle);

        VehicleResult result = vehicleService.create(command);

        assertNotNull(result);
        assertEquals(command.vin(), result.vin());
        assertEquals(command.plate(), result.plate());
        assertEquals(command.model(), result.model());
        assertEquals(command.manufacturer(), result.manufacturer());
        assertEquals(command.year(), result.year());
        assertEquals(command.ownerId(), result.ownerId());
    }

}