package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.model.Vehicle;
import com.github.diogocerqueiralima.infrastructure.entities.VehicleEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.VehicleMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleDataSourceImplTest {

    @Mock
    private VehicleMapper vehicleMapper;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehiclePersistenceImpl vehiclePersistence;

    @Test
    public void save_vehicle_should_succeed() {

        Vehicle toSave = new Vehicle(
                "VIN123", "AS-A2-GA", "2008", "Peugeot", 2020, UUID.randomUUID()
        );

        VehicleEntity toSaveEntity = new VehicleEntity();
        toSaveEntity.setVin(toSave.getVin());
        toSaveEntity.setPlate(toSave.getPlate());
        toSaveEntity.setModel(toSave.getModel());
        toSaveEntity.setManufacturer(toSave.getManufacturer());
        toSaveEntity.setYear(toSave.getYear());
        toSaveEntity.setOwnerId(toSave.getOwnerId());

        VehicleEntity savedEntity = new VehicleEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setVin(toSave.getVin());
        savedEntity.setPlate(toSave.getPlate());
        savedEntity.setModel(toSave.getModel());
        savedEntity.setManufacturer(toSave.getManufacturer());
        savedEntity.setYear(toSave.getYear());
        savedEntity.setOwnerId(toSave.getOwnerId());

        Vehicle expected = new Vehicle(
                savedEntity.getId(),
                savedEntity.getVin(),
                savedEntity.getPlate(),
                savedEntity.getModel(),
                savedEntity.getManufacturer(),
                savedEntity.getYear(),
                savedEntity.getOwnerId()
        );

        when(vehicleMapper.toEntity(toSave)).thenReturn(toSaveEntity);
        when(vehicleRepository.save(toSaveEntity)).thenReturn(savedEntity);
        when(vehicleMapper.toDomain(savedEntity)).thenReturn(expected);

        Vehicle actual = vehiclePersistence.save(toSave);

        assertEquals(expected, actual);
        verify(vehicleMapper).toEntity(toSave);
        verify(vehicleRepository).save(toSaveEntity);
        verify(vehicleMapper).toDomain(savedEntity);
    }

    @Test
    public void find_vehicle_by_id_should_succeed_if_exists() {

        UUID vehicleId = UUID.randomUUID();

        VehicleEntity foundEntity = new VehicleEntity();
        foundEntity.setId(vehicleId);
        foundEntity.setVin("VIN123");
        foundEntity.setPlate("AS-A2-GA");
        foundEntity.setModel("2008");
        foundEntity.setManufacturer("Peugeot");
        foundEntity.setYear(2020);
        foundEntity.setOwnerId(UUID.randomUUID());

        Vehicle expected = new Vehicle(
                foundEntity.getId(),
                foundEntity.getVin(),
                foundEntity.getPlate(),
                foundEntity.getModel(),
                foundEntity.getManufacturer(),
                foundEntity.getYear(),
                foundEntity.getOwnerId()
        );

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(foundEntity));
        when(vehicleMapper.toDomain(foundEntity)).thenReturn(expected);

        Optional<Vehicle> actual = vehiclePersistence.findById(vehicleId);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(vehicleRepository).findById(vehicleId);
        verify(vehicleMapper).toDomain(foundEntity);
    }

    @Test
    public void find_vehicle_by_id_should_return_empty_if_not_exists() {

        UUID vehicleId = UUID.randomUUID();

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        Optional<Vehicle> actual = vehiclePersistence.findById(vehicleId);

        assertFalse(actual.isPresent());
        verify(vehicleRepository).findById(vehicleId);
        verifyNoInteractions(vehicleMapper);
    }

    @Test
    public void delete_vehicle_should_succeed() {

        Vehicle vehicle = new Vehicle(
                UUID.randomUUID(),
                "VIN123",
                "AS-A2-GA",
                "2008",
                "Peugeot",
                2020,
                UUID.randomUUID()
        );

        VehicleEntity entity = new VehicleEntity();
        entity.setId(vehicle.getId());
        entity.setVin(vehicle.getVin());
        entity.setPlate(vehicle.getPlate());
        entity.setModel(vehicle.getModel());
        entity.setManufacturer(vehicle.getManufacturer());
        entity.setYear(vehicle.getYear());
        entity.setOwnerId(vehicle.getOwnerId());

        when(vehicleMapper.toEntity(vehicle)).thenReturn(entity);

        vehiclePersistence.delete(vehicle);

        verify(vehicleMapper).toEntity(vehicle);
        verify(vehicleRepository).delete(entity);
    }

    @Test
    public void exists_by_vin_should_return_true_if_exists() {

        String vin = "VIN123";

        when(vehicleRepository.existsByVin(vin)).thenReturn(true);

        boolean actual = vehiclePersistence.existsByVin(vin);

        assertTrue(actual);
        verify(vehicleRepository).existsByVin(vin);
    }

    @Test
    public void exists_by_vin_should_return_false_if_not_exists() {

        String vin = "VIN123";

        when(vehicleRepository.existsByVin(vin)).thenReturn(false);

        boolean actual = vehiclePersistence.existsByVin(vin);

        assertFalse(actual);
        verify(vehicleRepository).existsByVin(vin);
    }

    @Test
    public void exists_by_plate_should_return_true_if_exists() {

        String plate = "AS-A2-GA";

        when(vehicleRepository.existsByPlate(plate)).thenReturn(true);

        boolean actual = vehiclePersistence.existsByPlate(plate);

        assertTrue(actual);
        verify(vehicleRepository).existsByPlate(plate);
    }

    @Test
    public void exists_by_plate_should_return_false_if_not_exists() {

        String plate = "AS-A2-GA";

        when(vehicleRepository.existsByPlate(plate)).thenReturn(false);

        boolean actual = vehiclePersistence.existsByPlate(plate);

        assertFalse(actual);
        verify(vehicleRepository).existsByPlate(plate);

    }

}