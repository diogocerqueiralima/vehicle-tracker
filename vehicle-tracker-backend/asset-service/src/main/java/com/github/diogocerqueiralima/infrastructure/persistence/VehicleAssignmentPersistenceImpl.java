package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.assignments.VehicleAssignment;
import com.github.diogocerqueiralima.application.ports.outbound.VehicleAssignmentPersistence;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assets.VehicleEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assignments.VehicleAssignmentEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.DeviceMapper;
import com.github.diogocerqueiralima.infrastructure.mappers.VehicleAssignmentMapper;
import com.github.diogocerqueiralima.infrastructure.mappers.VehicleMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.VehicleAssignmentRepository;
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

        DeviceEntity deviceEntity = DeviceMapper.toEntity(vehicleAssignment.getDevice());
        VehicleEntity vehicleEntity = VehicleMapper.toEntity(vehicleAssignment.getVehicle());
        VehicleAssignmentEntity entity = toEntity(vehicleAssignment, deviceEntity, vehicleEntity);
        VehicleAssignmentEntity savedEntity = vehicleAssignmentRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public boolean existsActiveByDeviceId(UUID deviceId) {
        return vehicleAssignmentRepository.existsByDeviceIdAndUnassignedAtIsNull(deviceId);
    }

    @Override
    public boolean existsActiveByVehicleId(UUID vehicleId) {
        return vehicleAssignmentRepository.existsByVehicleIdAndUnassignedAtIsNull(vehicleId);
    }

    @Override
    public Optional<VehicleAssignment> findActiveByDeviceIdAndVehicleId(UUID deviceId, UUID vehicleId) {
        return vehicleAssignmentRepository.findByDeviceIdAndVehicleIdAndUnassignedAtIsNull(deviceId, vehicleId)
                .map(VehicleAssignmentMapper::toDomain);
    }
}

