package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.model.assignments.SimCardAssignment;
import com.github.diogocerqueiralima.domain.ports.outbound.SimCardAssignmentPersistence;
import com.github.diogocerqueiralima.infrastructure.entities.SimCardEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assignments.SimCardAssignmentEntity;
import com.github.diogocerqueiralima.infrastructure.mappers.DeviceMapper;
import com.github.diogocerqueiralima.infrastructure.mappers.SimCardMapper;
import com.github.diogocerqueiralima.infrastructure.repositories.SimCardAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.diogocerqueiralima.infrastructure.mappers.SimCardAssignmentMapper.toDomain;
import static com.github.diogocerqueiralima.infrastructure.mappers.SimCardAssignmentMapper.toEntity;

@Service
public class SimCardAssignmentPersistenceImpl implements SimCardAssignmentPersistence {

    private final SimCardAssignmentRepository simCardAssignmentRepository;

    public SimCardAssignmentPersistenceImpl(SimCardAssignmentRepository simCardAssignmentRepository) {
        this.simCardAssignmentRepository = simCardAssignmentRepository;
    }

    @Override
    @Transactional
    public SimCardAssignment save(SimCardAssignment simCardAssignment) {

        DeviceEntity deviceEntity = DeviceMapper.toEntity(simCardAssignment.getDevice());
        SimCardEntity simCardEntity = SimCardMapper.toEntity(simCardAssignment.getSimCard());
        SimCardAssignmentEntity entity = toEntity(simCardAssignment, deviceEntity, simCardEntity);
        SimCardAssignmentEntity savedEntity = simCardAssignmentRepository.save(entity);

        return toDomain(savedEntity);
    }

}

