package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.assignments.SimCardAssignment;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assets.SimCardEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assignments.SimCardAssignmentEntity;

/**
 * Mapper for SIM card assignment conversions between domain and persistence layers.
 */
public class SimCardAssignmentMapper {

    // Should not be instantiated
    private SimCardAssignmentMapper() {}

    /**
     *
     * Builds a persistence entity from a domain SIM card assignment and its related entities.
     *
     * @param assignment domain SIM card assignment with the data to be persisted.
     * @param deviceEntity persistence entity of the assigned device, required for the relationship.
     * @param simCardEntity persistence entity of the assigned SIM card, required for the relationship.
     * @return persistence entity with the provided data and relationships ready for persistence.
     */
    public static SimCardAssignmentEntity toEntity(
            SimCardAssignment assignment, DeviceEntity deviceEntity, SimCardEntity simCardEntity
    ) {

        SimCardAssignmentEntity entity = new SimCardAssignmentEntity();

        entity.setId(assignment.getId());
        entity.setDevice(deviceEntity);
        entity.setSimCard(simCardEntity);
        entity.setAssignedAt(assignment.getAssignedAt());
        entity.setUnassignedAt(assignment.getUnassignedAt());
        entity.setAssignedBy(assignment.getAssignedBy());
        entity.setUnassignedBy(assignment.getUnassignedBy());
        entity.setRemovalReason(assignment.getRemovalReason());

        return entity;
    }

    /**
     *
     * Builds a domain SIM card assignment from a persistence entity and its related entities.
     *
     * @param entity persistence entity with the SIM card assignment data and relationships.
     * @return domain SIM card assignment with the provided data and related domain objects.
     */
    public static SimCardAssignment toDomain(SimCardAssignmentEntity entity) {
        return new SimCardAssignment(
                entity.getId(),
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
