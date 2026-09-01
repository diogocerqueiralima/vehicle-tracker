package com.github.diogocerqueiralima.domain.vehicles.services

import com.github.diogocerqueiralima.domain.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.domain.vehicles.model.Vehicle
import com.github.diogocerqueiralima.domain.vehicles.repositories.VehicleRepository
import java.util.UUID

/**
 * Service responsible for handling vehicle-related use cases.
 */
class VehicleService(
    private val vehicleRepository: VehicleRepository
) {

    /**
     * Retrieves all vehicles.
     */
    suspend fun findAll(): List<Vehicle> = vehicleRepository.findAll()

    /**
     * Retrieves a vehicle by its unique identifier.
     *
     * @param id The unique identifier of the Vehicle.
     * @return The Vehicle with the given id.
     * @throws NotFoundException if no Vehicle with the given id exists.
     */
    suspend fun findById(id: UUID): Vehicle =
        vehicleRepository.findById(id) ?: throw NotFoundException(id)

}
