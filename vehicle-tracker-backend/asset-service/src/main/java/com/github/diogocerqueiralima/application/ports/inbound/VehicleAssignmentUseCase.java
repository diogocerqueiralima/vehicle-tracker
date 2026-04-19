package com.github.diogocerqueiralima.application.ports.inbound;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromVehicleCommand;
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

}

