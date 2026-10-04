package com.github.diogocerqueiralima.asset.service.infrastructure.repositories

import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.DeviceEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DeviceRepository : JpaRepository<DeviceEntity, UUID> {

    fun existsByIdAndOwnerId(id: UUID, ownerId: UUID): Boolean

    fun findByIdAndOwnerId(id: UUID, ownerId: UUID): DeviceEntity?

    fun findAllByOwnerId(ownerId: UUID, pageable: Pageable): Page<DeviceEntity>

    fun existsBySerialNumberOrImeiAndIdNot(serialNumber: String, imei: String, id: UUID): Boolean

}
