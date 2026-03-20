package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.assets.Vehicle;

import java.time.Instant;
import java.util.UUID;

/**
 * Mapper for vehicle conversions in the application layer.
 */
public final class VehicleApplicationMapper {

    private VehicleApplicationMapper() {}

    public static Vehicle toDomain(CreateVehicleCommand command, Instant now) {
        return new Vehicle(
                UUID.randomUUID(),
                now,
                now,
                command.vin(),
                command.plate(),
                command.model(),
                command.manufacturer(),
                command.manufacturingDate()
        );
    }

    public static Vehicle toDomain(UpdateVehicleCommand command, Vehicle existingVehicle, Instant updatedAt) {
        return new Vehicle(
                existingVehicle.getId(),
                existingVehicle.getCreatedAt(),
                updatedAt,
                command.vin(),
                command.plate(),
                command.model(),
                command.manufacturer(),
                command.manufacturingDate()
        );
    }

    public static VehicleResult toResult(Vehicle vehicle) {
        return new VehicleResult(
                vehicle.getId(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt(),
                vehicle.getVin(),
                vehicle.getPlate(),
                vehicle.getModel(),
                vehicle.getManufacturer(),
                vehicle.getManufacturingDate()
        );
    }

}

