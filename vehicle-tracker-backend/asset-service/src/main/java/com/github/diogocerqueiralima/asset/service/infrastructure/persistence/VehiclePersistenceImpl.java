package com.github.diogocerqueiralima.asset.service.infrastructure.persistence;

import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle;
import com.github.diogocerqueiralima.asset.service.domain.exceptions.VehicleAlreadyExistsException;
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehiclePersistence;
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.VehicleEntity;
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.VehicleMapper;
import com.github.diogocerqueiralima.asset.service.infrastructure.repositories.VehicleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.github.diogocerqueiralima.asset.service.infrastructure.mappers.VehicleMapper.toDomain;
import static com.github.diogocerqueiralima.asset.service.infrastructure.mappers.VehicleMapper.toEntity;

@Service
public class VehiclePersistenceImpl implements VehiclePersistence {

    private final VehicleRepository vehicleRepository;

    public VehiclePersistenceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {

        try {
            VehicleEntity entity = toEntity(vehicle);
            VehicleEntity savedEntity = vehicleRepository.save(entity);

            return toDomain(savedEntity);
        } catch (DataIntegrityViolationException e) {
            throw new VehicleAlreadyExistsException();
        }
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
    public boolean isOwner(UUID id, UUID ownerId) {
        return vehicleRepository.existsByIdAndOwnerId(id, ownerId);
    }

    /**
     * Retrieves a one-based pageNumber of vehicles from the data store.
     *
     * @param pageNumber one-based pageNumber number.
     * @param pageSize amount of items in the pageNumber.
     * @return paginated domain vehicles.
     */
    @Override
    public Page<Vehicle> getPage(int pageNumber, int pageSize) {

        // 1. Converts one-based inbound pageNumber number to Spring Data zero-based index.
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize);

        // 2. Loads entity pageNumber and maps each entry to the domain model.

        return vehicleRepository.findAll(pageRequest)
                .map(VehicleMapper::toDomain);
    }

    @Override
    public Page<Vehicle> getPageByOwnerId(int pageNumber, int pageSize, UUID ownerId) {

        // 1. Converts inbound pageNumber number to Spring Data zero-based index.
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        // 2. Loads owner-scoped entity pageNumber and maps each entry to the domain model.
        return vehicleRepository.findAllByOwnerId(ownerId, pageRequest)
                .map(VehicleMapper::toDomain);
    }

}

