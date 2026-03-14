package com.github.diogocerqueiralima.domain.model.assets;

import java.time.Instant;
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
        super(id, createdAt, updatedAt);
        this.serialNumber = serialNumber;
        this.model = model;
        this.manufacturer = manufacturer;
        this.imei = imei;
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
