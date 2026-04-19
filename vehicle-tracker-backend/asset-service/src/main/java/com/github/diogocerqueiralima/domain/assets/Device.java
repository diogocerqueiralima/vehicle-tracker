package com.github.diogocerqueiralima.domain.assets;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a device asset in the system.
 * The device is the physical hardware that can be assigned to a vehicle and have a SIM card installed.
 * It is used to track the {@link Vehicle}
 */
public class Device extends Asset {

    private final String serialNumber;
    private final String model;
    private final String manufacturer;
    private final String imei;

    public Device(UUID id, Instant createdAt, Instant updatedAt, String serialNumber, String model, String manufacturer, String imei) {
        this(id, null, createdAt, updatedAt, serialNumber, model, manufacturer, imei);
    }

    public Device(
            UUID id, UUID ownerId, Instant createdAt, Instant updatedAt, String serialNumber, String model,
            String manufacturer, String imei
    ) {
        super(id, ownerId, createdAt, updatedAt);
        this.serialNumber = Objects.requireNonNull(serialNumber, "serialNumber cannot be null");
        this.model = Objects.requireNonNull(model, "model cannot be null");
        this.manufacturer = Objects.requireNonNull(manufacturer, "manufacturer cannot be null");
        this.imei = Objects.requireNonNull(imei, "imei cannot be null");
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getModel() {
        return model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getImei() {
        return imei;
    }

}
