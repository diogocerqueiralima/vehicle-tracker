package com.github.diogocerqueiralima.domain.repositories

import com.github.diogocerqueiralima.domain.model.Vehicle
import java.util.UUID

/**
 * Repository interface for managing Vehicle entities.
 */
interface VehicleRepository {

    /**
     * Finds a Vehicle by its unique identifier.
     *
     * @param id The unique identifier of the Vehicle.
     * @return The Vehicle if found, or null if no Vehicle with the given ID exists.
     */
    suspend fun findById(id: UUID): Vehicle?

}