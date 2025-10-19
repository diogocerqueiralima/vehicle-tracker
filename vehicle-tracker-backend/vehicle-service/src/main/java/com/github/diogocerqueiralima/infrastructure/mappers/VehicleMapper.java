package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.Vehicle;
import com.github.diogocerqueiralima.infrastructure.entities.VehicleEntity;
import org.springframework.stereotype.Component;

@Component("infrastructure")
public class VehicleMapper {

    public VehicleEntity toEntity(Vehicle vehicle) {

        VehicleEntity vehicleEntity = new VehicleEntity();

        vehicleEntity.setId(vehicle.getId());
        vehicleEntity.setVin(vehicle.getVin());
        vehicleEntity.setPlate(vehicle.getPlate());
        vehicleEntity.setModel(vehicle.getModel());
        vehicleEntity.setManufacturer(vehicle.getManufacturer());
        vehicleEntity.setYear(vehicle.getYear());
        vehicleEntity.setOwnerId(vehicle.getOwnerId());

        return vehicleEntity;
    }

    public Vehicle toDomain(VehicleEntity vehicleEntity) {
        return new Vehicle(
                vehicleEntity.getId(),
                vehicleEntity.getVin(),
                vehicleEntity.getPlate(),
                vehicleEntity.getModel(),
                vehicleEntity.getManufacturer(),
                vehicleEntity.getYear(),
                vehicleEntity.getOwnerId()
        );
    }

}
