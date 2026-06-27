package com.github.diogocerqueiralima.asset.service.domain.ports.outbound;

import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardAssignment;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for sim card assignment persistence operations.
 * This interface defines methods for saving sim card assignments to a data store.
 */
public interface SimCardAssignmentPersistence {

    /**
     *
     * Saves a sim card assignment to the data store. If the sim card assignment already exists, it will be updated.
     *
     * @param simCardAssignment The sim card assignment to be saved or updated.
     * @return The saved or updated sim card assignment.
     */
    SimCardAssignment save(SimCardAssignment simCardAssignment);

    /**
     *
     * Finds an active assignment for a specific device and SIM card pair.
     *
     * @param deviceId device unique identifier.
     * @param simCardId SIM card id.
     * @return active assignment when found, otherwise empty.
     */
    Optional<SimCardAssignment> findActiveByDeviceIdAndSimCardId(UUID deviceId, UUID simCardId);

}