package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.assignments.SimCardAssignment;
import com.github.diogocerqueiralima.infrastructure.entities.SimCardEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assignments.SimCardAssignmentEntity;

public class SimCardAssignmentMapper {

    // Should not be instantiated
    private SimCardAssignmentMapper() {}

    public static SimCardAssignmentEntity toEntity(
            SimCardAssignment assignment, DeviceEntity deviceEntity, SimCardEntity simCardEntity
    ) {

        SimCardAssignmentEntity entity = new SimCardAssignmentEntity();

        entity.setDevice(deviceEntity);
        entity.setSimCard(simCardEntity);
        entity.setAssignedAt(assignment.getAssignedAt());
        entity.setUnassignedAt(assignment.getUnassignedAt());
        entity.setAssignedBy(assignment.getAssignedBy());
        entity.setUnassignedBy(assignment.getUnassignedBy());
        entity.setRemovalReason(assignment.getRemovalReason());

        return entity;
    }

    public static SimCardAssignment toDomain(SimCardAssignmentEntity entity) {
        return new SimCardAssignment(
                DeviceMapper.toDomain(entity.getDevice()),
                SimCardMapper.toDomain(entity.getSimCard()),
                entity.getAssignedAt(),
                entity.getUnassignedAt(),
                entity.getAssignedBy(),
                entity.getUnassignedBy(),
                entity.getRemovalReason()
        );
    }

}
