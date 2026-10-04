package com.github.diogocerqueiralima.asset.service.infrastructure.mappers

import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleAssignment
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.DeviceEntity
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.VehicleEntity
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assignments.VehicleAssignmentEntity

object VehicleAssignmentMapper {

    fun toEntity(
        assignment: VehicleAssignment, deviceEntity: DeviceEntity, vehicleEntity: VehicleEntity
    ): VehicleAssignmentEntity =
        VehicleAssignmentEntity().apply {
            id = assignment.id
            device = deviceEntity
            vehicle = vehicleEntity
            assignedAt = assignment.assignedAt
            unassignedAt = assignment.unassignedAt
            assignedBy = assignment.assignedBy
            unassignedBy = assignment.unassignedBy
            removalReason = assignment.removalReason
            installedBy = assignment.installedBy
            notes = assignment.notes
        }

    fun toDomain(entity: VehicleAssignmentEntity): VehicleAssignment =
        VehicleAssignment(
            entity.id,
            DeviceMapper.toDomain(entity.device),
            VehicleMapper.toDomain(entity.vehicle),
            entity.assignedAt,
            entity.unassignedAt,
            entity.assignedBy,
            entity.unassignedBy,
            entity.removalReason,
            entity.installedBy,
            entity.notes
        )

}
