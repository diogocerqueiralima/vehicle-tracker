package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.model.Vehicle;
import org.springframework.stereotype.Component;

@Component("application")
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

}
