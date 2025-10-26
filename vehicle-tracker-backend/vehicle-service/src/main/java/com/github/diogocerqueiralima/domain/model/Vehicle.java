package com.github.diogocerqueiralima.domain.model;

import java.util.Objects;
import java.util.UUID;

public class Vehicle {

    private final UUID id;
    private final String vin;
    private final String plate;
    private final String model;
    private final String manufacturer;
    private final int year;
    private final UUID ownerId;

    public Vehicle(UUID id, String vin, String plate, String model, String manufacturer, int year, UUID ownerId) {
        this.id = id;
        this.vin = vin;
        this.plate = plate;
        this.model = model;
        this.manufacturer = manufacturer;
        this.year = year;
        this.ownerId = ownerId;
    }

    public Vehicle(String vin, String plate, String model, String manufacturer, int year, UUID ownerId) {
        this(null, vin, plate, model, manufacturer, year, ownerId);
    }

    public boolean isOwnedBy(UUID userId) {
        return ownerId.equals(userId);
    }

    public String getDisplayName() {
        return String.format("%s - %s %s", plate, manufacturer, model);
    }

    public UUID getId() {
        return id;
    }

    public String getVin() {
        return vin;
    }

    public String getPlate() {
        return plate;
    }

    public String getModel() {
        return model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public int getYear() {
        return year;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(vin, vehicle.vin) && Objects.equals(plate, vehicle.plate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, plate);
    }

}
