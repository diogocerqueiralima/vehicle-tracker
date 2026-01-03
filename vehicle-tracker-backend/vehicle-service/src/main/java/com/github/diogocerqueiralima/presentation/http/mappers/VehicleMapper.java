package com.github.diogocerqueiralima.presentation.http.mappers;

import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.presentation.http.dto.VehicleDTO;
import org.springframework.stereotype.Component;

@Component("vm-presentation")
public class VehicleMapper {

    public VehicleDTO toDTO(VehicleResult vehicleResult) {
        return new VehicleDTO(
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
