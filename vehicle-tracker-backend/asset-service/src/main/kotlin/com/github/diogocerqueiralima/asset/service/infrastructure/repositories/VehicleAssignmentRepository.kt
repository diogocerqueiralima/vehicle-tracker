package com.github.diogocerqueiralima.asset.service.infrastructure.repositories

import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assignments.VehicleAssignmentEntity
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VehicleAssignmentRepository : JpaRepository<VehicleAssignmentEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByDeviceIdAndVehicleIdAndUnassignedAtIsNull(deviceId: UUID, vehicleId: UUID): VehicleAssignmentEntity?

    fun findByDeviceIdAndUnassignedAtIsNull(deviceId: UUID): VehicleAssignmentEntity?

    fun existsByDeviceIdAndUnassignedAtIsNull(deviceId: UUID): Boolean

    fun existsByVehicleIdAndUnassignedAtIsNull(vehicleId: UUID): Boolean

    @Query(
        value = """
            SELECT va.*
            FROM vehicle_assignments va
            JOIN assets a ON va.vehicle_id = a.id
            WHERE a.owner_id = :userId
            AND va.vehicle_id = :vehicleId
            """,
        nativeQuery = true
    )
    fun findHistory(vehicleId: UUID, userId: UUID, pageable: Pageable): Page<VehicleAssignmentEntity>

}
