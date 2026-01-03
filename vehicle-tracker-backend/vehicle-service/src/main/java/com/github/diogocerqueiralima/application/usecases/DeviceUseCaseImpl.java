package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.application.commands.LookupDeviceByIdCommand;
import com.github.diogocerqueiralima.application.commands.LookupVehicleByIdCommand;
import com.github.diogocerqueiralima.application.exceptions.DeviceNotFoundException;
import com.github.diogocerqueiralima.application.mappers.DeviceMapper;
import com.github.diogocerqueiralima.application.mappers.VehicleMapper;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.model.Device;
import com.github.diogocerqueiralima.domain.model.Vehicle;
import com.github.diogocerqueiralima.domain.ports.inbound.DeviceUseCase;
import com.github.diogocerqueiralima.domain.ports.inbound.VehicleUseCase;
import com.github.diogocerqueiralima.domain.ports.outbound.DevicePersistence;
import com.github.diogocerqueiralima.presentation.context.ExecutionContext;
import com.github.diogocerqueiralima.presentation.context.UserExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeviceUseCaseImpl implements DeviceUseCase {

    private final VehicleUseCase vehicleUseCase;
    private final DevicePersistence devicePersistence;
    private final DeviceMapper deviceMapper;
    private final VehicleMapper vehicleMapper;

    public DeviceUseCaseImpl(
            VehicleUseCase vehicleUseCase, DevicePersistence devicePersistence,
            @Qualifier("dm-application") DeviceMapper deviceMapper,
            @Qualifier("vm-application") VehicleMapper vehicleMapper
    ) {
        this.vehicleUseCase = vehicleUseCase;
        this.devicePersistence = devicePersistence;
        this.deviceMapper = deviceMapper;
        this.vehicleMapper = vehicleMapper;
    }

    @Override
    public DeviceResult create(CreateDeviceCommand command, ExecutionContext context) {

        VehicleResult vehicleResult = vehicleUseCase.getById(new LookupVehicleByIdCommand(command.vehicleId()), context);
        Vehicle vehicle = vehicleMapper.toDomain(vehicleResult);
        Device toSave = new Device(
                command.imei(),
                command.serialNumber(),
                command.manufacturer(),
                vehicle
        );

        Device savedDevice = devicePersistence.save(toSave);

        return deviceMapper.toResult(savedDevice);
    }

    @Override
    public DeviceResult getById(LookupDeviceByIdCommand command, ExecutionContext context) {
        Device device = getDeviceById(command.id(), context);
        return deviceMapper.toResult(device);
    }

    @Override
    public void delete(LookupDeviceByIdCommand command, ExecutionContext context) {
        Device device = getDeviceById(command.id(), context);
        devicePersistence.delete(device);
    }

    /**
     *
     * Retrieves a device by its ID.
     *
     * @param id the unique identifier of the device
     * @param context the execution context
     * @return the device with the specified ID
     * @throws DeviceNotFoundException if the device is not found
     */
    private Device getDeviceById(UUID id, ExecutionContext context) {

        Device device = devicePersistence.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        if (context.isUser()) {

            UserExecutionContext userExecutionContext = (UserExecutionContext) context;
            UUID userId = userExecutionContext.getUserId();
            Vehicle vehicle = device.getVehicle();

            if (!vehicle.isOwnedBy(userId)) {
                throw new DeviceNotFoundException(id);
            }

        }

        return device;
    }

}
