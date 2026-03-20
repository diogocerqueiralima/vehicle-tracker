package com.github.diogocerqueiralima.application.ports.outbound;

import com.github.diogocerqueiralima.domain.assets.Device;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for device persistence operations.
 * This interface defines methods for saving and retrieving devices from a data store.
 */
public interface DevicePersistence {

    /**
     *
     * Saves a device to the data store. If the device already exists, it will be updated.
     *
     * @param device The device to be saved or updated.
     * @return The saved or updated device.
     */
    Device save(Device device);

    /**
     *
     * Finds a device by its unique identifier.
     *
     * @param id The unique identifier of the device to be retrieved.
     * @return An Optional containing the device if found, or an empty Optional if not found.
     */
    Optional<Device> findById(UUID id);

}