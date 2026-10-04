package com.github.diogocerqueiralima.asset.service.infrastructure.persistence

import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehiclePersistence
import com.github.diogocerqueiralima.asset.service.infrastructure.mappers.VehicleMapper
import com.github.diogocerqueiralima.asset.service.infrastructure.repositories.VehicleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class VehiclePersistenceImpl(private val vehicleRepository: VehicleRepository) : VehiclePersistence {

    override fun save(vehicle: Vehicle): Vehicle {

        val entity = VehicleMapper.toEntity(vehicle)
        val savedEntity = vehicleRepository.save(entity)

        return VehicleMapper.toDomain(savedEntity)
    }

    override fun findById(id: UUID): Vehicle? =
        vehicleRepository.findByIdOrNull(id)?.let(VehicleMapper::toDomain)

    override fun findByIdAndOwnerId(id: UUID, ownerId: UUID): Vehicle? =
        vehicleRepository.findByIdAndOwnerId(id, ownerId)?.let(VehicleMapper::toDomain)

    override fun isOwner(id: UUID, ownerId: UUID): Boolean =
        vehicleRepository.existsByIdAndOwnerId(id, ownerId)

    override fun getPage(pageNumber: Int, pageSize: Int): Page<Vehicle> {

        // 1. Converts one-based inbound page number to Spring Data zero-based index.
        val pageRequest = PageRequest.of(pageNumber - 1, pageSize)

        // 2. Loads entity page and maps each entry to the domain model.
        return vehicleRepository.findAll(pageRequest).map { VehicleMapper.toDomain(it) }
    }

    override fun getPageByOwnerId(pageNumber: Int, pageSize: Int, ownerId: UUID): Page<Vehicle> {

        // 1. Converts inbound page number to Spring Data zero-based index.
        val pageRequest = PageRequest.of(pageNumber, pageSize)

        // 2. Loads owner-scoped entity page and maps each entry to the domain model.
        return vehicleRepository.findAllByOwnerId(ownerId, pageRequest).map { VehicleMapper.toDomain(it) }
    }

    override fun existsByVinOrPlate(vin: String, plate: String): Boolean =
        vehicleRepository.existsByVinOrPlate(vin, plate)

    override fun isVinOrPlateTakenByAnotherVehicle(vin: String, plate: String, excludingId: UUID): Boolean =
        vehicleRepository.existsByVinOrPlateAndIdNot(vin, plate, excludingId)

}
