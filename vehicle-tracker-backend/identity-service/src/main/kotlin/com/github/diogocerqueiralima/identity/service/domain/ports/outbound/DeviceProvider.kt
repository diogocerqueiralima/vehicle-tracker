package com.github.diogocerqueiralima.identity.service.domain.ports.outbound

import com.github.diogocerqueiralima.identity.service.domain.model.device.Device
import java.util.UUID

/**
 * Port to interact with the device items source.
 */
interface DeviceProvider {

    /**
     * @param id the unique identifier of the device
     * @return the [Device] with the given id, or null if no such device exists
     */
    fun findById(id: UUID): Device?

}
