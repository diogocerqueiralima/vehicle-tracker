package com.github.diogocerqueiralima.application.services;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.LookupVehicleByDeviceIdCommand;
import com.github.diogocerqueiralima.application.commands.LookupVehicleByIdCommand;
import com.github.diogocerqueiralima.application.exceptions.VehicleAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.application.mappers.VehicleMapper;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.model.Vehicle;
import com.github.diogocerqueiralima.domain.ports.inbound.VehicleService;
import com.github.diogocerqueiralima.domain.ports.outbound.VehiclePersistence;
import com.github.diogocerqueiralima.presentation.context.ExecutionContext;
import com.github.diogocerqueiralima.presentation.context.UserExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleMapper vehicleMapper;
    private final VehiclePersistence vehiclePersistence;

    public VehicleServiceImpl(
            @Qualifier("vm-application") VehicleMapper vehicleMapper, VehiclePersistence vehiclePersistence
    ) {
        this.vehicleMapper = vehicleMapper;
        this.vehiclePersistence = vehiclePersistence;
    }

    @Override
    public VehicleResult create(CreateVehicleCommand command) {

        if (vehiclePersistence.existsByVin(command.vin())) {
            throw new VehicleAlreadyExistsException("There is already a vehicle with the same VIN: " + command.vin());
        }

        if (vehiclePersistence.existsByPlate(command.plate())) {
            throw new VehicleAlreadyExistsException("There is already a vehicle with the same plate: " + command.plate());
        }

        Vehicle vehicle = vehiclePersistence.save(
                new Vehicle(
                        command.vin(),
                        command.plate(),
                        command.model(),
                        command.manufacturer(),
                        command.year(),
                        command.ownerId()
                )
        );

        return vehicleMapper.toResult(vehicle);
    }

    @Override
    public VehicleResult getById(LookupVehicleByIdCommand command, ExecutionContext context) {
        Vehicle vehicle = getById(command.id(), context);
        return vehicleMapper.toResult(vehicle);
    }

    @Override
    public VehicleResult getByDeviceId(LookupVehicleByDeviceIdCommand command) {

        Vehicle vehicle = vehiclePersistence.findByDeviceId(command.deviceId())
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with device ID " + command.deviceId() + " not found"));

        return vehicleMapper.toResult(vehicle);
    }

    @Override
    public void deleteById(LookupVehicleByIdCommand command, ExecutionContext context) {
        Vehicle vehicle = getById(command.id(), context);
        vehiclePersistence.delete(vehicle);
    }

    /**
     *
     * Retrieves a vehicle by its ID.
     *
     * @param id the unique identifier of the vehicle
     * @param context the execution context
     * @return the vehicle with the specified ID
     * @throws VehicleNotFoundException if the vehicle is not found
     */
    private Vehicle getById(UUID id, ExecutionContext context) {

        Vehicle vehicle = vehiclePersistence.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        if (context.isUser()) {

            UserExecutionContext userContext = (UserExecutionContext) context;
            UUID userId = userContext.getUserId();

            if (!vehicle.isOwnedBy(userId)) {
                throw new VehicleNotFoundException(id);
            }

        }

        return vehicle;
    }

}
