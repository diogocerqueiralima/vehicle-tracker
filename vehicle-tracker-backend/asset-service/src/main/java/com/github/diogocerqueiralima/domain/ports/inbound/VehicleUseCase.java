package com.github.diogocerqueiralima.domain.ports.inbound;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
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

}

