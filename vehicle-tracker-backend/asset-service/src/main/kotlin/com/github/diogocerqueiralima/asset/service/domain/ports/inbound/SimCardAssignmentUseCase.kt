package com.github.diogocerqueiralima.asset.service.domain.ports.inbound

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.results.SimCardAssignmentResult
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated

/**
 * Inbound port for SIM card assignment operations exposed to the presentation layer.
 */
@Validated
interface SimCardAssignmentUseCase {

    /**
     * Assigns a device to a SIM card.
     *
     * @param command assignment payload.
     * @return created assignment result.
     */
    fun assignDeviceToSimCard(@Valid command: AssignDeviceToSimCardCommand): SimCardAssignmentResult

    /**
     * Unassigns a device from a SIM card.
     *
     * @param command unassignment payload.
     * @return updated assignment result.
     */
    fun unassignDeviceFromSimCard(@Valid command: UnassignDeviceFromSimCardCommand): SimCardAssignmentResult

}
