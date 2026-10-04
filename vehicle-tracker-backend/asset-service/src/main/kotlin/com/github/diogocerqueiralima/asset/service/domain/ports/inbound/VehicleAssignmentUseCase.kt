package com.github.diogocerqueiralima.asset.service.domain.ports.inbound

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleAssignmentByDeviceIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleAssignmentHistoryCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated

/**
 * Inbound port for vehicle assignment operations exposed to the presentation layer.
 */
@Validated
interface VehicleAssignmentUseCase {

    /**
     * Assigns a device to a vehicle.
     *
     * @param command assignment payload.
     * @return created assignment result.
     */
    fun assignDeviceToVehicle(@Valid command: AssignDeviceToVehicleCommand): VehicleAssignmentResult

    /**
     * Unassigns a device from a vehicle.
     *
     * @param command unassignment payload.
     * @return updated assignment result.
     */
    fun unassignDeviceFromVehicle(@Valid command: UnassignDeviceFromVehicleCommand): VehicleAssignmentResult

    /**
     * Retrieves an active vehicle assignment by device id.
     *
     * @param command the get assignment by device id command.
     * @return the retrieved assignment result.
     */
    fun getVehicleAssignmentByDeviceId(@Valid command: GetVehicleAssignmentByDeviceIdCommand): VehicleAssignmentResult

    /**
     * Retrieves the assignment history of a vehicle.
     *
     * @param command the get vehicle assignment history command containing the vehicle id and user id for authorization.
     * @return the result containing the list of vehicle assignments representing the history of assignments for the specified vehicle.
     */
    fun getVehicleAssignmentHistory(@Valid command: GetVehicleAssignmentHistoryCommand): PageResult<VehicleAssignmentResult>

}
