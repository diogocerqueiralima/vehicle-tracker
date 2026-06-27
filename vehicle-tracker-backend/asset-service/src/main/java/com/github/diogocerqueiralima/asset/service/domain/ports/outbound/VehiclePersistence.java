package com.github.diogocerqueiralima.asset.service.domain.ports.outbound;

import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle;
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
     * Finds a vehicle by id constrained to the provided owner.
     *
     * @param id vehicle identifier.
     * @param ownerId owner identifier.
     * @return matching vehicle when found for the owner.
     */
    Optional<Vehicle> findByIdAndOwnerId(UUID id, UUID ownerId);

    /**
     *
     * Checks whether a vehicle with the provided id exists and is owned by the provided owner id.
     *
     * @param id vehicle identifier.
     * @param ownerId owner identifier.
     * @return true when the vehicle exists and is owned by the owner, otherwise false.
     */
    boolean isOwner(UUID id, UUID ownerId);

    /**
     * Retrieves a one-based pageNumber of vehicles.
     *
     * @param pageNumber one-based pageNumber number.
     * @param pageSize amount of items in the pageNumber.
     * @return paginated vehicles.
     */
    Page<Vehicle> getPage(int pageNumber, int pageSize);

    /**
     * Retrieves a one-based pageNumber of vehicles constrained to an owner.
     *
     * @param pageNumber one-based pageNumber number.
     * @param pageSize amount of items in the pageNumber.
     * @param ownerId owner identifier used to scope the search.
     * @return paginated vehicles for the owner.
     */
    Page<Vehicle> getPageByOwnerId(int pageNumber, int pageSize, UUID ownerId);

}