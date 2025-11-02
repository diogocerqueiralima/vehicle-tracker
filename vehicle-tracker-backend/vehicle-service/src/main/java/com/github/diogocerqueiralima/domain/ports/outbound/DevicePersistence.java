package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.Device;

import java.util.Optional;
import java.util.UUID;

/**
 * Port to interact with the device data source.
 */
public interface DevicePersistence {

    /**
     *
     * Saves a device to the data source.
     *
     * @param device the device to be saved
     * @return the saved device
     */
    Device save(Device device);

    /**
     *
     * Finds a device by its ID.
     *
     * @param id the ID of the device
     * @return an Optional containing the device if found, or empty if not found
     */
    Optional<Device> findById(UUID id);

    /**
     *
     * Deletes a device from the data source.
     *
     * @param device the device to be deleted
     */
    void delete(Device device);

}
