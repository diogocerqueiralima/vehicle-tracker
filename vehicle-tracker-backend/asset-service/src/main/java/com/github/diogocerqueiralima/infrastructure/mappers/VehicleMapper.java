package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.assets.Vehicle;
import com.github.diogocerqueiralima.infrastructure.entities.assets.VehicleEntity;

public class VehicleMapper {

    // Should not be instantiated
    private VehicleMapper() {}

    public static VehicleEntity toEntity(Vehicle vehicle) {

        VehicleEntity entity = new VehicleEntity();

        entity.setId(vehicle.getId());
        entity.setCreatedAt(vehicle.getCreatedAt());
        entity.setUpdatedAt(vehicle.getUpdatedAt());
        entity.setVin(vehicle.getVin());
        entity.setPlate(vehicle.getPlate());
        entity.setModel(vehicle.getModel());
        entity.setManufacturer(vehicle.getManufacturer());
        entity.setManufacturingDate(vehicle.getManufacturingDate());

        return entity;
    }

    public static Vehicle toDomain(VehicleEntity entity) {
        return new Vehicle(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVin(),
                entity.getPlate(),
                entity.getModel(),
                entity.getManufacturer(),
                entity.getManufacturingDate()
        );
    }

}
