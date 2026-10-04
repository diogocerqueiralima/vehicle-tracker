package com.github.diogocerqueiralima.asset.service.infrastructure.persistence

import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleAssignment
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehicleAssignmentPersistence
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.DeviceMapper
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.VehicleAssignmentMapper
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.VehicleMapper
import com.github.diogocerqueiralima.asset.service.infrastructure.repositories.VehicleAssignmentRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class VehicleAssignmentPersistenceImpl(
    private val vehicleAssignmentRepository: VehicleAssignmentRepository
) : VehicleAssignmentPersistence {

    override fun save(vehicleAssignment: VehicleAssignment): VehicleAssignment {

        val deviceEntity = DeviceMapper.toEntity(vehicleAssignment.device)
        val vehicleEntity = VehicleMapper.toEntity(vehicleAssignment.vehicle)

        val entity = VehicleAssignmentMapper.toEntity(vehicleAssignment, deviceEntity, vehicleEntity)
        val savedEntity = vehicleAssignmentRepository.save(entity)

        return VehicleAssignmentMapper.toDomain(savedEntity)
    }

    override fun findActiveByDeviceIdAndVehicleId(deviceId: UUID, vehicleId: UUID): VehicleAssignment? =
        vehicleAssignmentRepository.findByDeviceIdAndVehicleIdAndUnassignedAtIsNull(deviceId, vehicleId)
            ?.let(VehicleAssignmentMapper::toDomain)

    override fun findActiveByDeviceId(deviceId: UUID): VehicleAssignment? =
        vehicleAssignmentRepository.findByDeviceIdAndUnassignedAtIsNull(deviceId)
            ?.let(VehicleAssignmentMapper::toDomain)

    override fun findHistory(vehicleId: UUID, userId: UUID, pageNumber: Int, pageSize: Int): Page<VehicleAssignment> {

        val pageable = PageRequest.of(pageNumber, pageSize)

        return vehicleAssignmentRepository.findHistory(vehicleId, userId, pageable)
            .map { VehicleAssignmentMapper.toDomain(it) }
    }

    override fun hasActiveAssignmentForDevice(deviceId: UUID): Boolean =
        vehicleAssignmentRepository.existsByDeviceIdAndUnassignedAtIsNull(deviceId)

    override fun hasActiveAssignmentForVehicle(vehicleId: UUID): Boolean =
        vehicleAssignmentRepository.existsByVehicleIdAndUnassignedAtIsNull(vehicleId)

}
