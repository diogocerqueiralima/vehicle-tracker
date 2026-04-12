package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.assets.Vehicle;
import com.github.diogocerqueiralima.application.ports.outbound.VehiclePersistence;
import com.github.diogocerqueiralima.infrastructure.entities.assets.VehicleEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.VehicleMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.github.diogocerqueiralima.infrastructure.mappers.VehicleMapper.toDomain;
import static com.github.diogocerqueiralima.infrastructure.mappers.VehicleMapper.toEntity;

@Service
public class VehiclePersistenceImpl implements VehiclePersistence {

    private final VehicleRepository vehicleRepository;

    public VehiclePersistenceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {

        VehicleEntity entity = toEntity(vehicle);
        VehicleEntity savedEntity = vehicleRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return vehicleRepository
                .findById(id)
                .map(VehicleMapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByIdAndOwnerId(UUID id, UUID ownerId) {
        return vehicleRepository
                .findByIdAndOwnerId(id, ownerId)
                .map(VehicleMapper::toDomain);
    }

    @Override
    public boolean existsByVin(String vin) {
        return vehicleRepository.existsByVin(vin);
    }

    @Override
    public boolean existsByPlate(String plate) {
        return vehicleRepository.existsByPlate(plate);
    }

    /**
     * Retrieves a one-based page of vehicles from the data store.
     *
     * @param pageNumber one-based page number.
     * @param pageSize amount of items in the page.
     * @return paginated domain vehicles.
     */
    @Override
    public Page<Vehicle> getPage(int pageNumber, int pageSize) {

        // 1. Converts one-based inbound page number to Spring Data zero-based index.
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize);

        // 2. Loads entity page and maps each entry to the domain model.

        return vehicleRepository.findAll(pageRequest)
                .map(VehicleMapper::toDomain);
    }

    @Override
    public Page<Vehicle> getPageByOwnerId(int pageNumber, int pageSize, UUID ownerId) {

        // 1. Converts one-based inbound page number to Spring Data zero-based index.
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize);

        // 2. Loads owner-scoped entity page and maps each entry to the domain model.
        return vehicleRepository.findAllByOwnerId(ownerId, pageRequest)
                .map(VehicleMapper::toDomain);
    }

}

