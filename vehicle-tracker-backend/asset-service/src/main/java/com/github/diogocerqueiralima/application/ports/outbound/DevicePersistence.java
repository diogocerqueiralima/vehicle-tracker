package com.github.diogocerqueiralima.application.ports.outbound;

import com.github.diogocerqueiralima.domain.assets.Device;
import org.springframework.data.domain.Page;

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

    /**
     *
     * Checks whether a device with the provided serial number already exists.
     *
     * @param serialNumber The serial number to search for.
     * @return true if a device with the serial number exists, otherwise false.
     */
    boolean existsBySerialNumber(String serialNumber);

    /**
     *
     * Checks whether a device with the provided IMEI already exists.
     *
     * @param imei The IMEI to search for.
     * @return true if a device with the IMEI exists, otherwise false.
     */
    boolean existsByImei(String imei);

    /**
     * Retrieves a one-based page of devices.
     *
     * @param pageNumber one-based page number.
     * @param pageSize amount of items in the page.
     * @return paginated devices.
     */
    Page<Device> getPage(int pageNumber, int pageSize);

}