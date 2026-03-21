package com.github.diogocerqueiralima.application.ports.inbound;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleByIdCommand;
import com.github.diogocerqueiralima.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * Inbound port for vehicle operations exposed to the presentation layer.
 */
@Validated
public interface VehicleUseCase {

    /**
     * Creates a vehicle from the supplied command payload.
     *
     * @param command the create vehicle command.
     * @return the created vehicle result.
     */
    VehicleResult create(@Valid CreateVehicleCommand command);

    /**
     * Updates an existing vehicle identified by id.
     *
     * @param command the update vehicle command.
     * @return the updated vehicle result.
     */
    VehicleResult update(@Valid UpdateVehicleCommand command);

    /**
     * Retrieves an existing vehicle by id.
     *
     * @param command the get vehicle by id command.
     * @return the retrieved vehicle result.
     */
    VehicleResult getById(@Valid GetVehicleByIdCommand command);

}

