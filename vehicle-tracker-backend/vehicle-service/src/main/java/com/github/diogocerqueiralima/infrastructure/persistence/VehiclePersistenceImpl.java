package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.model.Vehicle;
import com.github.diogocerqueiralima.domain.ports.outbound.VehiclePersistence;
import com.github.diogocerqueiralima.infrastructure.entities.VehicleEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.VehicleMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.VehicleRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class VehiclePersistenceImpl implements VehiclePersistence {

    private final VehicleMapper vehicleMapper;
    private final VehicleRepository vehicleRepository;

    public VehiclePersistenceImpl(@Qualifier("vm-infrastructure") VehicleMapper vehicleMapper, VehicleRepository vehicleRepository) {
        this.vehicleMapper = vehicleMapper;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {

        VehicleEntity entity = vehicleMapper.toEntity(vehicle);
        VehicleEntity savedEntity = vehicleRepository.save(entity);

        return vehicleMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return vehicleRepository.findById(id)
                .map(vehicleMapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByDeviceId(UUID deviceId) {
        return vehicleRepository.findByDeviceId(deviceId)
                .map(vehicleMapper::toDomain);
    }

    @Override
    public void delete(Vehicle vehicle) {
        VehicleEntity entity = vehicleMapper.toEntity(vehicle);
        vehicleRepository.delete(entity);
    }

    @Override
    public boolean existsByVin(String vin) {
        return vehicleRepository.existsByVin(vin);
    }

    @Override
    public boolean existsByPlate(String plate) {
        return vehicleRepository.existsByPlate(plate);
    }

}