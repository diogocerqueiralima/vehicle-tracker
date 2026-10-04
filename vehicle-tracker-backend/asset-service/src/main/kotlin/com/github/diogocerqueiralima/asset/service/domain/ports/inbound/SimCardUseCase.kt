package com.github.diogocerqueiralima.asset.service.domain.ports.inbound

import com.github.diogocerqueiralima.asset.service.application.commands.CreateSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.DeleteSimCardByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetSimCardByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.results.SimCardResult
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated

/**
 * Inbound port for SIM card operations exposed to the presentation layer.
 */
@Validated
interface SimCardUseCase {

    /**
     * Creates a SIM card from the supplied command payload.
     *
     * @param command the create SIM card command.
     * @return the created SIM card result.
     */
    fun create(@Valid command: CreateSimCardCommand): SimCardResult

    /**
     * Updates an existing SIM card identified by id.
     *
     * @param command the update SIM card command.
     * @return the updated SIM card result.
     */
    fun update(@Valid command: UpdateSimCardCommand): SimCardResult

    /**
     * Retrieves an existing SIM card by id.
     *
     * @param command the get SIM card by id command.
     * @return the retrieved SIM card result.
     */
    fun getById(@Valid command: GetSimCardByIdCommand): SimCardResult

    /**
     * Deletes an existing SIM card by id.
     *
     * @param command the delete SIM card by id command.
     */
    fun deleteById(@Valid command: DeleteSimCardByIdCommand)

}
