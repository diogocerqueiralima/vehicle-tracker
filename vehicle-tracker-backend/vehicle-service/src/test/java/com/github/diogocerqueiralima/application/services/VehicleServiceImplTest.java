package com.github.diogocerqueiralima.application.services;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.LookupVehicleByIdCommand;
import com.github.diogocerqueiralima.application.exceptions.VehicleAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.application.mappers.VehicleMapper;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.model.Vehicle;
import com.github.diogocerqueiralima.domain.ports.outbound.VehicleDataSource;
import com.github.diogocerqueiralima.presentation.context.InternalExecutionContext;
import com.github.diogocerqueiralima.presentation.context.UserExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
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

    @Test
    public void get_vehicle_by_id_that_does_not_exist_should_throw_exception() {

        UUID vehicleId = UUID.randomUUID();

        when(vehicleDataSource.findById(vehicleId))
                .thenReturn(Optional.empty());

        assertThrows(VehicleNotFoundException.class, () -> {
            vehicleService.getById(new LookupVehicleByIdCommand(vehicleId), InternalExecutionContext.create("system"));
        });
    }

    @Test
    public void get_vehicle_by_id_that_exists_but_not_owned_by_user_should_throw_exception() {

        UUID vehicleId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Vehicle vehicle = new Vehicle(
                vehicleId,
                "1HGCM82633A123456",
                "AB-12-C3",
                "Civic",
                "Honda",
                2020,
                ownerId
        );

        when(vehicleDataSource.findById(vehicleId))
                .thenReturn(Optional.of(vehicle));

        assertThrows(VehicleNotFoundException.class, () -> {
            vehicleService.getById(new LookupVehicleByIdCommand(vehicleId), UserExecutionContext.create(otherUserId));
        });
    }

    @Test
    public void get_vehicle_by_id_that_exists_and_owned_by_user_should_succeed() {

        UUID vehicleId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Vehicle vehicle = new Vehicle(
                vehicleId,
                "1HGCM82633A123456",
                "AB-12-C3",
                "Civic",
                "Honda",
                2020,
                ownerId
        );

        when(vehicleDataSource.findById(vehicleId))
                .thenReturn(Optional.of(vehicle));

        VehicleResult result = vehicleService.getById(
                new LookupVehicleByIdCommand(vehicleId),
                UserExecutionContext.create(ownerId)
        );

        assertNotNull(result);
        assertEquals(vehicleId, result.id());
        assertEquals(vehicle.getVin(), result.vin());
        assertEquals(vehicle.getPlate(), result.plate());
        assertEquals(vehicle.getModel(), result.model());
        assertEquals(vehicle.getManufacturer(), result.manufacturer());
        assertEquals(vehicle.getYear(), result.year());
        assertEquals(vehicle.getOwnerId(), result.ownerId());
    }

    @Test
    public void get_vehicle_by_id_as_internal_context_should_succeed() {

        UUID vehicleId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Vehicle vehicle = new Vehicle(
                vehicleId,
                "1HGCM82633A123456",
                "AB-12-C3",
                "Civic",
                "Honda",
                2020,
                ownerId
        );

        when(vehicleDataSource.findById(vehicleId))
                .thenReturn(Optional.of(vehicle));

        VehicleResult result = vehicleService.getById(
                new LookupVehicleByIdCommand(vehicleId),
                InternalExecutionContext.create("system")
        );

        assertNotNull(result);
        assertEquals(vehicleId, result.id());
        assertEquals(vehicle.getVin(), result.vin());
        assertEquals(vehicle.getPlate(), result.plate());
        assertEquals(vehicle.getModel(), result.model());
        assertEquals(vehicle.getManufacturer(), result.manufacturer());
        assertEquals(vehicle.getYear(), result.year());
        assertEquals(vehicle.getOwnerId(), result.ownerId());
    }

}