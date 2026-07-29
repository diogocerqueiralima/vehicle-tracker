package com.github.diogocerqueiralima.domain.services

import com.github.diogocerqueiralima.domain.exceptions.VehicleNotFoundException
import com.github.diogocerqueiralima.domain.model.Vehicle
import com.github.diogocerqueiralima.domain.repositories.VehicleRepository
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
     * @throws VehicleNotFoundException if no Vehicle with the given id exists.
     */
    suspend fun findById(id: UUID): Vehicle =
        vehicleRepository.findById(id) ?: throw VehicleNotFoundException(id)

}
