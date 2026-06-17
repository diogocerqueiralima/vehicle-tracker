package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.assignments.VehicleAssignment;

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

}