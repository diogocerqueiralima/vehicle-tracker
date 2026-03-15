package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.assignments.VehicleAssignment;

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

}