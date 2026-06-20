package com.github.diogocerqueiralima.domain.ports.inbound;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleAssignmentByDeviceIdCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleAssignmentHistoryCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentHistoryResult;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * Inbound port for vehicle assignment operations exposed to the presentation layer.
 */
@Validated
public interface VehicleAssignmentUseCase {

    /**
     * Assigns a device to a vehicle.
     *
     * @param command assignment payload.
     * @return created assignment result.
     */
    VehicleAssignmentResult assignDeviceToVehicle(@Valid AssignDeviceToVehicleCommand command);

    /**
     * Unassigns a device from a vehicle.
     *
     * @param command unassignment payload.
     * @return updated assignment result.
     */
    VehicleAssignmentResult unassignDeviceFromVehicle(@Valid UnassignDeviceFromVehicleCommand command);

    /**
     *
     * Retrieves an active vehicle assignment by device id.
     *
     * @param command the get assignment by device id command.
     * @return the retrieved assignment result.
     */
    VehicleAssignmentResult getVehicleAssignmentByDeviceId(@Valid GetVehicleAssignmentByDeviceIdCommand command);

    /**
     *
     * Retrieves the assignment history of a vehicle.
     *
     * @param command the get vehicle assignment history command containing the vehicle id and user id for authorization.
     * @return the result containing the list of vehicle assignments representing the history of assignments for the specified vehicle.
     */
    VehicleAssignmentHistoryResult getVehicleAssignmentHistory(@Valid GetVehicleAssignmentHistoryCommand command);

}

