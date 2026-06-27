package com.github.diogocerqueiralima.asset.service.infrastructure.persistence;

import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleAssignment;
import com.github.diogocerqueiralima.asset.service.domain.exceptions.VehicleAssignmentFailedException;
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehicleAssignmentPersistence;
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.VehicleEntity;
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assignments.VehicleAssignmentEntity;
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.DeviceMapper;
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.VehicleAssignmentMapper;
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.VehicleMapper;
import com.github.diogocerqueiralima.asset.service.infrastructure.repositories.VehicleAssignmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.github.diogocerqueiralima.asset.service.infrastructure.mappers.VehicleAssignmentMapper.toDomain;
import static com.github.diogocerqueiralima.asset.service.infrastructure.mappers.VehicleAssignmentMapper.toEntity;

@Service
public class VehicleAssignmentPersistenceImpl implements VehicleAssignmentPersistence {

    private final VehicleAssignmentRepository vehicleAssignmentRepository;

    public VehicleAssignmentPersistenceImpl(VehicleAssignmentRepository vehicleAssignmentRepository) {
        this.vehicleAssignmentRepository = vehicleAssignmentRepository;
    }

    @Override
    public VehicleAssignment save(VehicleAssignment vehicleAssignment) {

        try {
            DeviceEntity deviceEntity = DeviceMapper.toEntity(vehicleAssignment.getDevice());
            VehicleEntity vehicleEntity = VehicleMapper.toEntity(vehicleAssignment.getVehicle());
            VehicleAssignmentEntity entity = toEntity(vehicleAssignment, deviceEntity, vehicleEntity);
            VehicleAssignmentEntity savedEntity = vehicleAssignmentRepository.save(entity);

            return toDomain(savedEntity);
        } catch (DataIntegrityViolationException e) {
            throw new VehicleAssignmentFailedException(
                    vehicleAssignment.getDevice().getId(),
                    vehicleAssignment.getVehicle().getId()
            );
        }
    }

    @Override
    public Optional<VehicleAssignment> findActiveByDeviceIdAndVehicleId(UUID deviceId, UUID vehicleId) {
        return vehicleAssignmentRepository.findByDeviceIdAndVehicleIdAndUnassignedAtIsNull(deviceId, vehicleId)
                .map(VehicleAssignmentMapper::toDomain);
    }

    @Override
    public Optional<VehicleAssignment> findActiveByDeviceId(UUID deviceId) {
        return vehicleAssignmentRepository.findByDeviceIdAndUnassignedAtIsNull(deviceId)
                .map(VehicleAssignmentMapper::toDomain);
    }

    @Override
    public Page<VehicleAssignment> findHistory(UUID vehicleId, UUID userId, int pageNumber, int pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return vehicleAssignmentRepository.findHistory(vehicleId, userId, pageable)
                .map(VehicleAssignmentMapper::toDomain);
    }

}

