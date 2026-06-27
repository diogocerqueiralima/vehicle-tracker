package com.github.diogocerqueiralima.identity.service.domain.model.device;

import java.util.UUID;

/**
 * Represents a device in the system.
 */
public class Device {

    private final UUID id;
    private final UUID ownerId;

    /**
     *
     * Creates a new Device with the given id and ownerId.
     *
     * @param id the unique identifier of the device
     * @param ownerId the unique identifier of the owner of the device
     */
    public Device(UUID id, UUID ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

}
