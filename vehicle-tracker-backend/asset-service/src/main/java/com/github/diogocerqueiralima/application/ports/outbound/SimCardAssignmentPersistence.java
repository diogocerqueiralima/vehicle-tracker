package com.github.diogocerqueiralima.application.ports.outbound;

import com.github.diogocerqueiralima.domain.assignments.SimCardAssignment;

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

}