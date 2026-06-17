package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.assignments.VehicleAssignment;
import com.github.diogocerqueiralima.domain.exceptions.VehicleAssignmentFailedException;
import com.github.diogocerqueiralima.domain.ports.outbound.VehicleAssignmentPersistence;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assets.VehicleEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assignments.VehicleAssignmentEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.DeviceMapper;
import com.github.diogocerqueiralima.infrastructure.mappers.VehicleAssignmentMapper;
import com.github.diogocerqueiralima.infrastructure.mappers.VehicleMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.VehicleAssignmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.github.diogocerqueiralima.infrastructure.mappers.VehicleAssignmentMapper.toDomain;
import static com.github.diogocerqueiralima.infrastructure.mappers.VehicleAssignmentMapper.toEntity;

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

}

