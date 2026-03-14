package com.github.diogocerqueiralima.domain.model.assets;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a vehicle asset in the system.
 * A vehicle has a VIN, plate number, model, manufacturer, and manufacturing date.
 */
public class Vehicle extends Asset {

    private final String vin;
    private final String plate;
    private final String model;
    private final String manufacturer;
    private final LocalDate manufacturingDate;

    public Vehicle(
            UUID id, Instant createdAt, Instant updatedAt, String vin, String plate,
            String model, String manufacturer, LocalDate manufacturingDate
    ) {
        super(id, createdAt, updatedAt);
        this.vin = vin;
        this.plate = plate;
        this.model = model;
        this.manufacturer = manufacturer;
        this.manufacturingDate = manufacturingDate;
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

    public LocalDate getManufacturingDate() {
        return manufacturingDate;
    }

}
