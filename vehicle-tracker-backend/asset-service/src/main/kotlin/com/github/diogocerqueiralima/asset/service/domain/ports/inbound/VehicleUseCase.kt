package com.github.diogocerqueiralima.asset.service.domain.ports.inbound

import com.github.diogocerqueiralima.asset.service.application.commands.CreateVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehiclePageCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.application.results.VehicleResult
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated

/**
 * Inbound port for vehicle operations exposed to the presentation layer.
 */
@Validated
interface VehicleUseCase {

    /**
     * Creates a vehicle from the supplied command payload.
     *
     * @param command the create vehicle command.
     * @return the created vehicle result.
     */
    fun create(@Valid command: CreateVehicleCommand): VehicleResult

    /**
     * Updates an existing vehicle identified by id.
     *
     * @param command the update vehicle command.
     * @return the updated vehicle result.
     */
    fun update(@Valid command: UpdateVehicleCommand): VehicleResult

    /**
     * Retrieves an existing vehicle by id.
     *
     * @param command the get vehicle by id command.
     * @return the retrieved vehicle result.
     */
    fun getById(@Valid command: GetVehicleByIdCommand): VehicleResult

    /**
     * Retrieves a one-based page of vehicles.
     *
     * @param command the page request command.
     * @return paginated vehicle result.
     */
    fun getPage(@Valid command: GetVehiclePageCommand): PageResult<VehicleResult>

}
