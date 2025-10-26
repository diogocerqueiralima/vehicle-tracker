package com.github.diogocerqueiralima.domain.ports.inbound;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.LookupVehicleByIdCommand;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.presentation.context.ExecutionContext;
import com.github.diogocerqueiralima.application.exceptions.VehicleAlreadyExistsException;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * Service interface for vehicle-related operations.
 */
@Validated
public interface VehicleService {

    /**
     *
     * Creates a new vehicle based on the provided command.
     *
     * @param command the command containing vehicle details
     * @return the result of the vehicle creation
     * @throws VehicleAlreadyExistsException If there is any vehicle with the same VIN or plate
     */
    VehicleResult create(@Valid CreateVehicleCommand command);

    /**
     *
     * Retrieves a vehicle by its ID based on the provided command.
     *
     * @param command the command containing the vehicle ID
     * @param context the execution context
     * @return the result of the vehicle lookup
     * @throws VehicleNotFoundException if the vehicle is not found
     */
    VehicleResult getById(@Valid LookupVehicleByIdCommand command, ExecutionContext context);

    /**
     *
     * Deletes a vehicle by its ID based on the provided command.
     *
     * @param command the command containing the vehicle ID
     * @param context the execution context
     */
    void deleteById(@Valid LookupVehicleByIdCommand command, ExecutionContext context);

}
