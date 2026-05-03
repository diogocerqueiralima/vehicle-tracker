package com.github.diogocerqueiralima.application.ports.inbound;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.DeleteSimCardByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetSimCardByIdCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * Inbound port for SIM card operations exposed to the presentation layer.
 */
@Validated
public interface SimCardUseCase {

    /**
     * Creates a SIM card from the supplied command payload.
     *
     * @param command the create SIM card command.
     * @return the created SIM card result.
     */
    SimCardResult create(@Valid CreateSimCardCommand command);

    /**
     * Updates an existing SIM card identified by id.
     *
     * @param command the update SIM card command.
     * @return the updated SIM card result.
     */
    SimCardResult update(@Valid UpdateSimCardCommand command);

    /**
     * Retrieves an existing SIM card by id.
     *
     * @param command the get SIM card by id command.
     * @return the retrieved SIM card result.
     */
    SimCardResult getById(@Valid GetSimCardByIdCommand command);

    /**
     * Deletes an existing SIM card by id.
     *
     * @param command the delete SIM card by id command.
     */
    void deleteById(@Valid DeleteSimCardByIdCommand command);

}

