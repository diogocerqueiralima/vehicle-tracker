package com.github.diogocerqueiralima.application.ports.outbound;

import com.github.diogocerqueiralima.domain.assets.Vehicle;
import org.springframework.data.domain.Page;

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

    /**
     *
     * Checks whether a vehicle with the provided VIN already exists.
     *
     * @param vin The VIN to search for.
     * @return true if a vehicle with the VIN exists, otherwise false.
     */
    boolean existsByVin(String vin);

    /**
     *
     * Checks whether a vehicle with the provided plate already exists.
     *
     * @param plate The plate to search for.
     * @return true if a vehicle with the plate exists, otherwise false.
     */
    boolean existsByPlate(String plate);

    /**
     * Retrieves a one-based page of vehicles.
     *
     * @param pageNumber one-based page number.
     * @param pageSize amount of items in the page.
     * @return paginated vehicles.
     */
    Page<Vehicle> getPage(int pageNumber, int pageSize);

}