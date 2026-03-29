package com.github.diogocerqueiralima.infrastructure.entities.assets;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@PrimaryKeyJoinColumn(name = "id")
public class VehicleEntity extends AssetEntity {

    @Column(name = "vin", nullable = false, unique = true)
    private String vin;

    @Column(name = "plate", nullable = false, unique = true)
    private String plate;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "manufacturer", nullable = false)
    private String manufacturer;

    @Column(name = "manufacturing_date", nullable = false)
    private LocalDate manufacturingDate;

    public VehicleEntity() {}

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public LocalDate getManufacturingDate() {
        return manufacturingDate;
    }

    public void setManufacturingDate(LocalDate manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
    }

}

