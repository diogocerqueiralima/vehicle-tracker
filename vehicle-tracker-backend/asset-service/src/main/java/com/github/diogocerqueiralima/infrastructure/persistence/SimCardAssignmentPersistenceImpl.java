package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.assignments.SimCardAssignment;
import com.github.diogocerqueiralima.domain.exceptions.SimCardAssignmentFailedException;
import com.github.diogocerqueiralima.domain.ports.outbound.SimCardAssignmentPersistence;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assets.SimCardEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assignments.SimCardAssignmentEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.DeviceMapper;
import com.github.diogocerqueiralima.infrastructure.mappers.SimCardAssignmentMapper;
import com.github.diogocerqueiralima.infrastructure.mappers.SimCardMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.SimCardAssignmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.github.diogocerqueiralima.infrastructure.mappers.SimCardAssignmentMapper.toDomain;
import static com.github.diogocerqueiralima.infrastructure.mappers.SimCardAssignmentMapper.toEntity;

@Service
public class SimCardAssignmentPersistenceImpl implements SimCardAssignmentPersistence {

    private final SimCardAssignmentRepository simCardAssignmentRepository;

    public SimCardAssignmentPersistenceImpl(SimCardAssignmentRepository simCardAssignmentRepository) {
        this.simCardAssignmentRepository = simCardAssignmentRepository;
    }

    @Override
    public SimCardAssignment save(SimCardAssignment simCardAssignment) {

        try {
            DeviceEntity deviceEntity = DeviceMapper.toEntity(simCardAssignment.getDevice());
            SimCardEntity simCardEntity = SimCardMapper.toEntity(simCardAssignment.getSimCard());
            SimCardAssignmentEntity entity = toEntity(simCardAssignment, deviceEntity, simCardEntity);
            SimCardAssignmentEntity savedEntity = simCardAssignmentRepository.save(entity);

            return toDomain(savedEntity);
        } catch (DataIntegrityViolationException e) {
            throw new SimCardAssignmentFailedException(
                    simCardAssignment.getDevice().getId(),
                    simCardAssignment.getSimCard().getId()
            );
        }
    }

    @Override
    public Optional<SimCardAssignment> findActiveByDeviceIdAndSimCardId(UUID deviceId, UUID simCardId) {
        return simCardAssignmentRepository.findByDeviceIdAndSimCardIdAndUnassignedAtIsNull(deviceId, simCardId)
                .map(SimCardAssignmentMapper::toDomain);
    }

}

