package com.github.diogocerqueiralima.asset.service.infrastructure.persistence

import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardAssignment
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.SimCardAssignmentPersistence
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.DeviceMapper
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.SimCardAssignmentMapper
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.SimCardMapper
import com.github.diogocerqueiralima.asset.service.infrastructure.repositories.SimCardAssignmentRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SimCardAssignmentPersistenceImpl(
    private val simCardAssignmentRepository: SimCardAssignmentRepository
) : SimCardAssignmentPersistence {

    override fun save(simCardAssignment: SimCardAssignment): SimCardAssignment {

        val deviceEntity = DeviceMapper.toEntity(simCardAssignment.device)
        val simCardEntity = SimCardMapper.toEntity(simCardAssignment.simCard)

        val entity = SimCardAssignmentMapper.toEntity(simCardAssignment, deviceEntity, simCardEntity)
        val savedEntity = simCardAssignmentRepository.save(entity)

        return SimCardAssignmentMapper.toDomain(savedEntity)
    }

    override fun findActiveByDeviceIdAndSimCardId(deviceId: UUID, simCardId: UUID): SimCardAssignment? =
        simCardAssignmentRepository.findByDeviceIdAndSimCardIdAndUnassignedAtIsNull(deviceId, simCardId)
            ?.let(SimCardAssignmentMapper::toDomain)

    override fun hasActiveAssignmentForDevice(deviceId: UUID): Boolean =
        simCardAssignmentRepository.existsByDeviceIdAndUnassignedAtIsNull(deviceId)

    override fun hasActiveAssignmentForSimCard(simCardId: UUID): Boolean =
        simCardAssignmentRepository.existsBySimCardIdAndUnassignedAtIsNull(simCardId)

}
