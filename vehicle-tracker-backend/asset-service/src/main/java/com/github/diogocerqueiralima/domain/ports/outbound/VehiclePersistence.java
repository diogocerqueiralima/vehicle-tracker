package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.assets.Vehicle;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for vehicle persistence operations.
 * This interface defines methods for saving and retrieving vehicles from a data store.
 */
public interface VehiclePersistence {

    /**
     *
     * Saves a vehicle to the data store. If the vehicle already exists, it will be updated.
     *
     * @param vehicle The vehicle to be saved or updated.
     * @return The saved or updated vehicle.
     */
    Vehicle save(Vehicle vehicle);

    /**
     *
     * Finds a vehicle by its unique identifier.
     *
     * @param id The unique identifier of the vehicle to be retrieved.
     * @return An Optional containing the vehicle if found, or an empty Optional if not found.
     */
    Optional<Vehicle> findById(UUID id);

}