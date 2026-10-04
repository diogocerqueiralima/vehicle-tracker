package com.github.diogocerqueiralima.asset.service.infrastructure.repositories

import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.VehicleEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VehicleRepository : JpaRepository<VehicleEntity, UUID> {

    fun existsByIdAndOwnerId(id: UUID, ownerId: UUID): Boolean

    fun findByIdAndOwnerId(id: UUID, ownerId: UUID): VehicleEntity?

    fun findAllByOwnerId(ownerId: UUID, pageable: Pageable): Page<VehicleEntity>

    fun existsByVinOrPlate(vin: String, plate: String): Boolean

    fun existsByVinOrPlateAndIdNot(vin: String, plate: String, id: UUID): Boolean

}
