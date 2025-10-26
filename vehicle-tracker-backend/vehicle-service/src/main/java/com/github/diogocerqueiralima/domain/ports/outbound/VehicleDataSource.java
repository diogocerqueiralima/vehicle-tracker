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

    /**
     *
     * Deletes the given vehicle from the data source.
     *
     * @param vehicle the vehicle to be deleted
     */
    void delete(Vehicle vehicle);

    /**
     *
     * Checks if a vehicle exists by its VIN.
     *
     * @param vin the vehicle identification number of the vehicle
     * @return true if a vehicle with the given VIN exists, false otherwise
     */
    boolean existsByVin(String vin);

    /**
     *
     * Checks if a vehicle exists by its plate.
     *
     * @param plate the plate of the vehicle
     * @return true if a vehicle with the given plate exists, false otherwise
     */
    boolean existsByPlate(String plate);

}
