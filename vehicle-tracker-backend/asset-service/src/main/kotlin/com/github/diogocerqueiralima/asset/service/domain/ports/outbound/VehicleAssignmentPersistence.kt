package com.github.diogocerqueiralima.asset.service.domain.ports.outbound

import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleAssignment
import org.springframework.data.domain.Page
import java.util.UUID

/**
 * Interface for vehicle assignment persistence operations.
 * This interface defines methods for saving vehicle assignments to a data store.
 */
interface VehicleAssignmentPersistence {

    /**
     * Saves a vehicle assignment to the data store. If the vehicle assignment already exists, it will be updated.
     *
     * @param vehicleAssignment The vehicle assignment to be saved or updated.
     * @return The saved or updated vehicle assignment.
     */
    fun save(vehicleAssignment: VehicleAssignment): VehicleAssignment

    /**
     * Finds an active assignment for a specific device and vehicle pair.
     *
     * @param deviceId device unique identifier.
     * @param vehicleId vehicle unique identifier.
     * @return active assignment when found, otherwise null.
     */
    fun findActiveByDeviceIdAndVehicleId(deviceId: UUID, vehicleId: UUID): VehicleAssignment?

    /**
     * Finds an active assignment for a specific device.
     *
     * @param deviceId device unique identifier.
     * @return active assignment when found, otherwise null.
     */
    fun findActiveByDeviceId(deviceId: UUID): VehicleAssignment?

    /**
     * Finds the history of assignments for a specific vehicle and user, returning a paginated result.
     *
     * @param vehicleId The unique identifier of the vehicle for which to retrieve the assignment history.
     * @param userId The unique identifier of the user who owns the vehicle.
     * @param pageNumber zero-based page number.
     * @param pageSize amount of items in the page.
     * @return A paginated list of [VehicleAssignment] instances representing the assignment history of the specified vehicle for the given user.
     */
    fun findHistory(vehicleId: UUID, userId: UUID, pageNumber: Int, pageSize: Int): Page<VehicleAssignment>

    /**
     * Checks whether the given device already has an active assignment.
     *
     * @param deviceId device unique identifier.
     * @return true when an active assignment exists for the device.
     */
    fun hasActiveAssignmentForDevice(deviceId: UUID): Boolean

    /**
     * Checks whether the given vehicle already has an active assignment.
     *
     * @param vehicleId vehicle unique identifier.
     * @return true when an active assignment exists for the vehicle.
     */
    fun hasActiveAssignmentForVehicle(vehicleId: UUID): Boolean

}
