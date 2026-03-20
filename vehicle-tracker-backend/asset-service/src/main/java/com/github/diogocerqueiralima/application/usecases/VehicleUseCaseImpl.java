package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.application.exceptions.VehicleAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.application.mappers.VehicleApplicationMapper;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.assets.Vehicle;
import com.github.diogocerqueiralima.application.ports.inbound.VehicleUseCase;
import com.github.diogocerqueiralima.application.ports.outbound.VehiclePersistence;
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
        Vehicle vehicle = VehicleApplicationMapper.toDomain(command, now);

        // 4. Saves the vehicle
        Vehicle savedVehicle = vehiclePersistence.save(vehicle);

        // 5. Build the result
        return VehicleApplicationMapper.toResult(savedVehicle);
    }

    @Override
    public VehicleResult update(UpdateVehicleCommand command) {

        UUID id = command.id();

        // 1. Get the vehicle with the provided id
        Vehicle existingVehicle = vehiclePersistence.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        // 2. Check if exists a vehicle with the given VIN
        if (!existingVehicle.getVin().equals(command.vin()) && vehiclePersistence.existsByVin(command.vin())) {
            throw new VehicleAlreadyExistsException("A vehicle with the provided VIN already exists.");
        }

        // 3. Check if exists a vehicle with the given plate
        if (!existingVehicle.getPlate().equals(command.plate()) && vehiclePersistence.existsByPlate(command.plate())) {
            throw new VehicleAlreadyExistsException("A vehicle with the provided plate already exists.");
        }

        // 4. Update the vehicle
        Vehicle vehicleToSave = VehicleApplicationMapper.toDomain(command, existingVehicle, Instant.now());

        // 5. Save the vehicle
        Vehicle updatedVehicle = vehiclePersistence.save(vehicleToSave);

        // 6. Build the result
        return VehicleApplicationMapper.toResult(updatedVehicle);
    }

}

