package com.github.diogocerqueiralima.application.ports.outbound;

import com.github.diogocerqueiralima.domain.assignments.SimCardAssignment;

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
     * Checks whether a device already has an active SIM card assignment.
     *
     * @param deviceId device unique identifier.
     * @return true when the device has an active assignment.
     */
    boolean existsActiveByDeviceId(UUID deviceId);

    /**
     *
     * Checks whether a SIM card already has an active device assignment.
     *
     * @param id SIM card id.
     * @return true when the SIM card has an active assignment.
     */
    boolean existsActiveBySimCardId(UUID id);

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