package com.github.diogocerqueiralima.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Device {

    private final UUID id;
    private final String imei;
    private final String serialNumber;
    private final String manufacturer;
    private final Instant installedAt;
    private final Instant lastSeen;
    private final Vehicle vehicle;

    public Device(
            UUID id, String imei, String serialNumber, String manufacturer,
            Instant installedAt, Instant lastSeen, Vehicle vehicle
    ) {
        this.id = id;
        this.imei = imei;
        this.serialNumber = serialNumber;
        this.manufacturer = manufacturer;
        this.installedAt = installedAt;
        this.lastSeen = lastSeen;
        this.vehicle = vehicle;
    }

    public UUID getId() {
        return id;
    }

    public String getImei() {
        return imei;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public Instant getInstalledAt() {
        return installedAt;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(imei, device.imei) && Objects.equals(serialNumber, device.serialNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imei, serialNumber);
    }

}
