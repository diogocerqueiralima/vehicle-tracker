package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.device.Device;

import java.util.Optional;
import java.util.UUID;

/**
 * Port to interact with the device items source.
 */
public interface DeviceProvider {

    /**
     * @param id the unique identifier of the device
     * @return an Optional containing the {@link Device} with the given id, or an empty Optional if no such device exists
     */
    Optional<Device> findById(UUID id);

}
