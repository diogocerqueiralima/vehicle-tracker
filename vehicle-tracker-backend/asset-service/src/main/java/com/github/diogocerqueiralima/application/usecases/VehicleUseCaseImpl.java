package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.exceptions.VehicleAlreadyExistsException;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.model.assets.Vehicle;
import com.github.diogocerqueiralima.domain.ports.inbound.VehicleUseCase;
import com.github.diogocerqueiralima.domain.ports.outbound.VehiclePersistence;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Application-layer implementation that orchestrates vehicle creation.
 */
@Service
public class VehicleUseCaseImpl implements VehicleUseCase {

    private final VehiclePersistence vehiclePersistence;

    public VehicleUseCaseImpl(VehiclePersistence vehiclePersistence) {
        this.vehiclePersistence = vehiclePersistence;
    }

    @Override
    public VehicleResult create(CreateVehicleCommand command) {

        // 1. Checks if exists a vehicle with the provided vin
        if (vehiclePersistence.existsByVin(command.vin())) {
            throw new VehicleAlreadyExistsException("A vehicle with the provided VIN already exists.");
        }

        // 2. Checks if exists a vehicle with the provided plate
        if (vehiclePersistence.existsByPlate(command.plate())) {
            throw new VehicleAlreadyExistsException("A vehicle with the provided plate already exists.");
        }

        // 3. Create a new vehicle
        Instant now = Instant.now();
        Vehicle vehicle = new Vehicle(
                UUID.randomUUID(),
                now,
                now,
                command.vin(),
                command.plate(),
                command.model(),
                command.manufacturer(),
                command.manufacturingDate()
        );

        // 4. Saves the vehicle
        Vehicle savedVehicle = vehiclePersistence.save(vehicle);

        // 5. Build the result and return it
        return new VehicleResult(
                savedVehicle.getId(),
                savedVehicle.getCreatedAt(),
                savedVehicle.getUpdatedAt(),
                savedVehicle.getVin(),
                savedVehicle.getPlate(),
                savedVehicle.getModel(),
                savedVehicle.getManufacturer(),
                savedVehicle.getManufacturingDate()
        );
    }

}

