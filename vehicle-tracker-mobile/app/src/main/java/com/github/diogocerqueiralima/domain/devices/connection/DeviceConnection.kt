package com.github.diogocerqueiralima.domain.devices.connection

/**
 * Interface for operations related to device connections.
 */
interface DeviceConnection {

    /**
     * Connects to a device with the given [address].
     *
     * @param address The address of the device to connect to.
     */
    fun connect(address: String)

}