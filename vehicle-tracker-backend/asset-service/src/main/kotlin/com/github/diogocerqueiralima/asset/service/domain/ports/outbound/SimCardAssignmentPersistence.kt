package com.github.diogocerqueiralima.asset.service.domain.ports.outbound

import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardAssignment
import java.util.UUID

/**
 * Interface for sim card assignment persistence operations.
 * This interface defines methods for saving sim card assignments to a data store.
 */
interface SimCardAssignmentPersistence {

    /**
     * Saves a sim card assignment to the data store. If the sim card assignment already exists, it will be updated.
     *
     * @param simCardAssignment The sim card assignment to be saved or updated.
     * @return The saved or updated sim card assignment.
     */
    fun save(simCardAssignment: SimCardAssignment): SimCardAssignment

    /**
     * Finds an active assignment for a specific device and SIM card pair.
     *
     * @param deviceId device unique identifier.
     * @param simCardId SIM card id.
     * @return active assignment when found, otherwise null.
     */
    fun findActiveByDeviceIdAndSimCardId(deviceId: UUID, simCardId: UUID): SimCardAssignment?

    /**
     * Checks whether the given device already has an active assignment.
     *
     * @param deviceId device unique identifier.
     * @return true when an active assignment exists for the device.
     */
    fun hasActiveAssignmentForDevice(deviceId: UUID): Boolean

    /**
     * Checks whether the given SIM card already has an active assignment.
     *
     * @param simCardId SIM card unique identifier.
     * @return true when an active assignment exists for the SIM card.
     */
    fun hasActiveAssignmentForSimCard(simCardId: UUID): Boolean

}
