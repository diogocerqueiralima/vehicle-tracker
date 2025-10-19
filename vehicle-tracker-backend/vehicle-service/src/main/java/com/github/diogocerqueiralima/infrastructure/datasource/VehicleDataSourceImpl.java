package com.github.diogocerqueiralima.infrastructure.datasource;

import com.github.diogocerqueiralima.domain.model.Vehicle;
import com.github.diogocerqueiralima.domain.ports.outbound.VehicleDataSource;
import com.github.diogocerqueiralima.infrastructure.entities.VehicleEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.VehicleMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.VehicleRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class VehicleDataSourceImpl implements VehicleDataSource {

    private final VehicleMapper vehicleMapper;
    private final VehicleRepository vehicleRepository;

    public VehicleDataSourceImpl(@Qualifier("infrastructure") VehicleMapper vehicleMapper, VehicleRepository vehicleRepository) {
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

}