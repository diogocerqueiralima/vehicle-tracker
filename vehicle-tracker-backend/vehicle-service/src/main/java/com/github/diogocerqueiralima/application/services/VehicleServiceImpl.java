package com.github.diogocerqueiralima.application.services;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.LookupVehicleByIdCommand;
import com.github.diogocerqueiralima.application.exceptions.VehicleAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.application.mappers.VehicleMapper;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.model.Vehicle;
import com.github.diogocerqueiralima.domain.ports.inbound.VehicleService;
import com.github.diogocerqueiralima.domain.ports.outbound.VehicleDataSource;
import com.github.diogocerqueiralima.presentation.context.ExecutionContext;
import com.github.diogocerqueiralima.presentation.context.UserExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleMapper vehicleMapper;
    private final VehicleDataSource vehicleDataSource;

    public VehicleServiceImpl(@Qualifier("application") VehicleMapper vehicleMapper, VehicleDataSource vehicleDataSource) {
        this.vehicleMapper = vehicleMapper;
        this.vehicleDataSource = vehicleDataSource;
    }

    @Override
    public VehicleResult create(CreateVehicleCommand command) {

        if (vehicleDataSource.existsByVin(command.vin())) {
            throw new VehicleAlreadyExistsException("There is already a vehicle with the same VIN: " + command.vin());
        }

        if (vehicleDataSource.existsByPlate(command.plate())) {
            throw new VehicleAlreadyExistsException("There is already a vehicle with the same plate: " + command.plate());
        }

        Vehicle vehicle = vehicleDataSource.save(
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

        Vehicle vehicle = vehicleDataSource.findById(command.id())
                .orElseThrow(() -> new VehicleNotFoundException(command.id()));

        if (context.isUser()) {

            UserExecutionContext userContext = (UserExecutionContext) context;
            UUID userId = userContext.getUserId();

            if (!vehicle.getOwnerId().equals(userId))
                throw new VehicleNotFoundException(command.id());

        }

        return vehicleMapper.toResult(vehicle);
    }

}
