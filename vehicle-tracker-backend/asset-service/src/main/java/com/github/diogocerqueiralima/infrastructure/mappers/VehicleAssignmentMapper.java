package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.assignments.VehicleAssignment;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assets.VehicleEntity;
import com.github.diogocerqueiralima.infrastructure.entities.assignments.VehicleAssignmentEntity;

public class VehicleAssignmentMapper {

    // Should not be instantiated
    private VehicleAssignmentMapper() {}

    public static VehicleAssignmentEntity toEntity(
            VehicleAssignment assignment, DeviceEntity deviceEntity, VehicleEntity vehicleEntity
    ) {

        VehicleAssignmentEntity entity = new VehicleAssignmentEntity();

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

    public static VehicleAssignment toDomain(VehicleAssignmentEntity entity) {
        return new VehicleAssignment(
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
