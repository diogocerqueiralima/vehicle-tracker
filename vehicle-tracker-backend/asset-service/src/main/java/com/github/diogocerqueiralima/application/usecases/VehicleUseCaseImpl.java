package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetVehiclePageCommand;
import com.github.diogocerqueiralima.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.application.exceptions.VehicleAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.application.mappers.VehicleApplicationMapper;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.assets.Vehicle;
import com.github.diogocerqueiralima.application.ports.inbound.VehicleUseCase;
import com.github.diogocerqueiralima.application.ports.outbound.VehiclePersistence;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Application-layer implementation that orchestrates vehicle use cases.
 */
@Service
public class VehicleUseCaseImpl implements VehicleUseCase {

    private static final int MAX_PAGE_SIZE = 50;

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

    /**
     * Retrieves a vehicle by id.
     *
     * @param command get-by-id payload.
     * @return the matching vehicle as a result object.
     */
    @Override
    public VehicleResult getById(GetVehicleByIdCommand command) {

        // 1. Resolve the target id directly from the inbound command.
        UUID id = command.id();

        // 2. Load and fail fast when the vehicle does not exist.
        Vehicle vehicle = vehiclePersistence.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        // 3. Map the domain object to the response contract.
        return VehicleApplicationMapper.toResult(vehicle);
    }

    /**
     * Retrieves a one-based page of vehicles.
     *
     * @param command page request payload.
     * @return paginated vehicle result.
     */
    @Override
    public PageResult<VehicleResult> getPage(GetVehiclePageCommand command) {

        // 1. Get the params used to search
        int pageNumber = command.pageNumber();
        int pageSize = command.pageSize();

        // 2. Fetches the page from persistence preserving one-based indexing semantics.
        Page<Vehicle> vehiclePageResult = vehiclePersistence.getPage(pageNumber, pageSize);

        // 3. Converts domain page payload to application output contract.
        return VehicleApplicationMapper.toPageResult(vehiclePageResult);
    }

}

