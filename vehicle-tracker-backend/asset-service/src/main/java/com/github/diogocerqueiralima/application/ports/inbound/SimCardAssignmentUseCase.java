package com.github.diogocerqueiralima.application.ports.inbound;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardAssignmentResult;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * Inbound port for SIM card assignment operations exposed to the presentation layer.
 */
@Validated
public interface SimCardAssignmentUseCase {

    /**
     * Assigns a device to a SIM card.
     *
     * @param command assignment payload.
     * @return created assignment result.
     */
    SimCardAssignmentResult assignDeviceToSimCard(@Valid AssignDeviceToSimCardCommand command);

    /**
     * Unassigns a device from a SIM card.
     *
     * @param command unassignment payload.
     * @return updated assignment result.
     */
    SimCardAssignmentResult unassignDeviceFromSimCard(@Valid UnassignDeviceFromSimCardCommand command);

}

