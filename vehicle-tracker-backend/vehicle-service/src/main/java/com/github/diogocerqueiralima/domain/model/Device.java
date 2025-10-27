package com.github.diogocerqueiralima.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Device {

    private final UUID id;
    private final String imei;
    private final String serialNumber;
    private final String manufacturer;
    private final Vehicle vehicle;

    public Device(UUID id, String imei, String serialNumber, String manufacturer, Vehicle vehicle) {
        this.id = id;
        this.imei = imei;
        this.serialNumber = serialNumber;
        this.manufacturer = manufacturer;
        this.vehicle = vehicle;
    }

    public Device(String imei, String serialNumber, String manufacturer, Vehicle vehicle) {
        this(null, imei, serialNumber, manufacturer, vehicle);
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
