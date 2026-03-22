package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.assets.Vehicle;
import com.github.diogocerqueiralima.infrastructure.entities.assets.VehicleEntity;

/**
 * Mapper for vehicle conversions between domain and persistence layers.
 */
public class VehicleMapper {

    // Should not be instantiated
    private VehicleMapper() {}

    /**
     *
     * Builds a persistence entity from a domain vehicle.
     *
     * @param vehicle domain vehicle with the data to be persisted.
     * @return persistence entity with the provided data and timestamps.
     */
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

    /**
     *
     * Builds a domain vehicle from a persistence entity.
     *
     * @param entity persistence entity with the vehicle data.
     * @return domain vehicle with the provided data and timestamps.
     */
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
