package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.Vehicle;

import java.util.Optional;
import java.util.UUID;

/**
 * Port to interact with the vehicle data source.
 */
public interface VehicleDataSource {

    /**
     *
     * Saves the given vehicle to the data source.
     *
     * @param vehicle the vehicle to be saved
     * @return the saved vehicle
     */
    Vehicle save(Vehicle vehicle);

    /**
     *
     * Gets a vehicle by its ID.
     *
     * @param id the ID of the vehicle
     * @return an Optional containing the vehicle if found, or empty if not found
     */
    Optional<Vehicle> findById(UUID id);

}
