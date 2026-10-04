package com.github.diogocerqueiralima.asset.service.domain.ports.inbound

import com.github.diogocerqueiralima.asset.service.application.commands.CreateOrUpdateDeviceCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetDevicePageCommand
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated

/**
 * Inbound port for device operations exposed to the presentation layer.
 */
@Validated
interface DeviceUseCase {

    /**
     * Creates a new device or updates an existing one, both identified by the id supplied by the client.
     *
     * @param command the create-or-update device command.
     * @return the saved device result.
     */
    fun createOrUpdate(@Valid command: CreateOrUpdateDeviceCommand): DeviceResult

    /**
     * Retrieves an existing device by id.
     *
     * @param command the get device by id command.
     * @return the retrieved device result.
     * @throws com.github.diogocerqueiralima.error.common.exceptions.NotFoundException if the device is not found.
     */
    fun getById(@Valid command: GetDeviceByIdCommand): DeviceResult

    /**
     * Retrieves a one-based page of devices.
     *
     * @param command the page request command.
     * @return paginated device result.
     */
    fun getPage(@Valid command: GetDevicePageCommand): PageResult<DeviceResult>

}
