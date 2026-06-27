package com.github.diogocerqueiralima.asset.service.application.usecases;

import com.github.diogocerqueiralima.asset.service.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehiclePageCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.asset.service.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.asset.service.application.mappers.VehicleApplicationMapper;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import com.github.diogocerqueiralima.asset.service.application.results.VehicleResult;
import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle;
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.VehicleUseCase;
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehiclePersistence;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Application-layer implementation that orchestrates vehicle use cases.
 */
@Service
public class VehicleUseCaseImpl implements VehicleUseCase {

    private final VehiclePersistence vehiclePersistence;

    public VehicleUseCaseImpl(VehiclePersistence vehiclePersistence) {
        this.vehiclePersistence = vehiclePersistence;
    }

    @Override
    public VehicleResult create(CreateVehicleCommand command) {

        // 1. Create a new vehicle
        Instant now = Instant.now();
        Vehicle vehicle = VehicleApplicationMapper.toDomain(command, now);

        // 4. Saves the vehicle
        Vehicle savedVehicle = vehiclePersistence.save(vehicle);

        // 5. Build the result
        return VehicleApplicationMapper.toResult(savedVehicle);
    }

    @Override
    @Transactional
    public VehicleResult update(UpdateVehicleCommand command) {

        UUID id = command.id();
        UUID userId = command.userId();

        // 1. Gets the vehicle by id constrained to the authenticated owner.
        Vehicle existingVehicle = vehiclePersistence.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        // 2. Update the vehicle
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
        UUID userId = command.userId();

        // 2. Load by id constrained to owner and fail fast when absent.
        Vehicle vehicle = vehiclePersistence.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        // 3. Map the domain object to the response contract.
        return VehicleApplicationMapper.toResult(vehicle);
    }

    /**
     * Retrieves a one-based pageNumber of vehicles.
     *
     * @param command pageNumber request payload.
     * @return paginated vehicle result.
     */
    @Override
    public PageResult<VehicleResult> getPage(GetVehiclePageCommand command) {

        // 1. Get the params used to search
        int pageNumber = command.pageNumber();
        int pageSize = command.pageSize();
        UUID userId = command.userId();

        // 2. Fetches the owner-scoped pageNumber from persistence preserving one-based indexing semantics.
        Page<Vehicle> vehiclePageResult = vehiclePersistence.getPageByOwnerId(pageNumber - 1, pageSize, userId);

        // 3. Converts domain pageNumber payload to application output contract.
        return VehicleApplicationMapper.toPageResult(vehiclePageResult);
    }

}

