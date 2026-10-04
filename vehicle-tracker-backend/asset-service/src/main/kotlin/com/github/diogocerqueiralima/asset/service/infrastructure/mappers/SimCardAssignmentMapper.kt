package com.github.diogocerqueiralima.asset.service.infrastructure.mappers

import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardAssignment
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.DeviceEntity
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.SimCardEntity
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assignments.SimCardAssignmentEntity

object SimCardAssignmentMapper {

    fun toEntity(
        assignment: SimCardAssignment, deviceEntity: DeviceEntity, simCardEntity: SimCardEntity
    ): SimCardAssignmentEntity =
        SimCardAssignmentEntity().apply {
            id = assignment.id
            device = deviceEntity
            simCard = simCardEntity
            assignedAt = assignment.assignedAt
            unassignedAt = assignment.unassignedAt
            assignedBy = assignment.assignedBy
            unassignedBy = assignment.unassignedBy
            removalReason = assignment.removalReason
        }

    fun toDomain(entity: SimCardAssignmentEntity): SimCardAssignment =
        SimCardAssignment(
            entity.id,
            DeviceMapper.toDomain(entity.device),
            SimCardMapper.toDomain(entity.simCard),
            entity.assignedAt,
            entity.unassignedAt,
            entity.assignedBy,
            entity.unassignedBy,
            entity.removalReason
        )

}
