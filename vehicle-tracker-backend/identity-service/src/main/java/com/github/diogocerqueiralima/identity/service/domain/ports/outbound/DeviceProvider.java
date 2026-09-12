package com.github.diogocerqueiralima.identity.service.domain.ports.outbound;

import com.github.diogocerqueiralima.identity.service.domain.model.device.Device;

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

    /**
     *
     * Checks whether a device belongs to a given user. A device that does not exist is not owned by
     * anyone, so it is reported the same way as one owned by somebody else: the caller is told only
     * whether it may act on the device, not whether it is there.
     *
     * @param deviceId the unique identifier of the device whose ownership is being checked
     * @param userId the unique identifier of the user the device is expected to belong to
     * @return true if the device exists and belongs to the user, false otherwise
     */
    boolean isOwnedBy(UUID deviceId, UUID userId);

}
