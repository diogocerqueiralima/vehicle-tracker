package com.github.diogocerqueiralima.asset.service.domain.ports.outbound;

import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleAssignment;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for vehicle assignment persistence operations.
 * This interface defines methods for saving vehicle assignments to a data store.
 */
public interface VehicleAssignmentPersistence {

    /**
     *
     * Saves a vehicle assignment to the data store. If the vehicle assignment already exists, it will be updated.
     *
     * @param vehicleAssignment The vehicle assignment to be saved or updated.
     * @return The saved or updated vehicle assignment.
     */
    VehicleAssignment save(VehicleAssignment vehicleAssignment);

    /**
     *
     * Finds an active assignment for a specific device and vehicle pair.
     *
     * @param deviceId device unique identifier.
     * @param vehicleId vehicle unique identifier.
     * @return active assignment when found, otherwise empty.
     */
    Optional<VehicleAssignment> findActiveByDeviceIdAndVehicleId(UUID deviceId, UUID vehicleId);

    /**
     *
     * Finds an active assignment for a specific device.
     *
     * @param deviceId device unique identifier.
     * @return active assignment when found, otherwise empty.
     */
    Optional<VehicleAssignment> findActiveByDeviceId(UUID deviceId);

    /**
     *
     * Finds the history of assignments for a specific vehicle and user, returning a paginated result.
     *
     * @param vehicleId The unique identifier of the vehicle for which to retrieve the assignment history.
     * @param userId The unique identifier of the user who owns the vehicle.
     * @param pageNumber zero-based pageNumber number.
     * @param pageSize amount of items in the pageNumber.
     * @return A paginated list of {@link VehicleAssignment} instances representing the assignment history of the specified vehicle for the given user.
     */
    Page<VehicleAssignment> findHistory(UUID vehicleId, UUID userId, int pageNumber, int pageSize);

}