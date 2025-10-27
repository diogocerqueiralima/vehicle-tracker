package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.model.Vehicle;
import org.springframework.stereotype.Component;

@Component("vm-application")
public class VehicleMapper {

    public VehicleResult toResult(Vehicle vehicle) {
        return new VehicleResult(
                vehicle.getId(),
                vehicle.getVin(),
                vehicle.getPlate(),
                vehicle.getModel(),
                vehicle.getManufacturer(),
                vehicle.getYear(),
                vehicle.getOwnerId()
        );
    }

    public Vehicle toDomain(VehicleResult vehicleResult) {
        return new Vehicle(
                vehicleResult.id(),
                vehicleResult.vin(),
                vehicleResult.plate(),
                vehicleResult.model(),
                vehicleResult.manufacturer(),
                vehicleResult.year(),
                vehicleResult.ownerId()
        );
    }

}
