package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.assignments.VehicleAssignment;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assets.VehicleEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assignments.VehicleAssignmentEntity;

/**
 * Mapper for vehicle assignment conversions between domain and persistence layers.
 */
public class VehicleAssignmentMapper {

    // Should not be instantiated
    private VehicleAssignmentMapper() {}

    /**
     *
     * Builds a persistence entity from a domain vehicle assignment and its related entities.
     *
     * @param assignment domain vehicle assignment with the data to be persisted.
     * @param deviceEntity persistence entity of the assigned device, required for the relationship.
     * @param vehicleEntity persistence entity of the assigned vehicle, required for the relationship.
     * @return persistence entity with the provided data and relationships ready for persistence.
     */
    public static VehicleAssignmentEntity toEntity(
            VehicleAssignment assignment, DeviceEntity deviceEntity, VehicleEntity vehicleEntity
    ) {

        VehicleAssignmentEntity entity = new VehicleAssignmentEntity();

        entity.setId(assignment.getId());
        entity.setDevice(deviceEntity);
        entity.setVehicle(vehicleEntity);
        entity.setAssignedAt(assignment.getAssignedAt());
        entity.setUnassignedAt(assignment.getUnassignedAt());
        entity.setAssignedBy(assignment.getAssignedBy());
        entity.setUnassignedBy(assignment.getUnassignedBy());
        entity.setRemovalReason(assignment.getRemovalReason());
        entity.setInstalledBy(assignment.getInstalledBy());
        entity.setNotes(assignment.getNotes());

        return entity;
    }

    /**
     *
     * Builds a domain vehicle assignment from a persistence entity and its related entities.
     *
     * @param entity persistence entity with the vehicle assignment data and relationships.
     * @return domain vehicle assignment with the provided data and related domain objects.
     */
    public static VehicleAssignment toDomain(VehicleAssignmentEntity entity) {
        return new VehicleAssignment(
                entity.getId(),
                DeviceMapper.toDomain(entity.getDevice()),
                VehicleMapper.toDomain(entity.getVehicle()),
                entity.getAssignedAt(),
                entity.getUnassignedAt(),
                entity.getAssignedBy(),
                entity.getUnassignedBy(),
                entity.getRemovalReason(),
                entity.getInstalledBy(),
                entity.getNotes()
        );
    }


}
