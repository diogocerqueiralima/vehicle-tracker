package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.application.commands.UpdateDeviceCommand;
import com.github.diogocerqueiralima.application.exceptions.DeviceAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.DeviceNotFoundException;
import com.github.diogocerqueiralima.application.mappers.DeviceApplicationMapper;
import com.github.diogocerqueiralima.application.ports.inbound.DeviceUseCase;
import com.github.diogocerqueiralima.application.ports.outbound.DevicePersistence;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.domain.assets.Device;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Application-layer implementation that orchestrates device use cases.
 */
@Service
public class DeviceUseCaseImpl implements DeviceUseCase {

    private final DevicePersistence devicePersistence;

    public DeviceUseCaseImpl(DevicePersistence devicePersistence) {
        this.devicePersistence = devicePersistence;
    }

    @Override
    public DeviceResult create(CreateDeviceCommand command) {

        // 1. Checks if exists a device with the provided serial number.
        if (devicePersistence.existsBySerialNumber(command.serialNumber())) {
            throw new DeviceAlreadyExistsException("A device with the provided serial number already exists.");
        }

        // 2. Checks if exists a device with the provided IMEI.
        if (devicePersistence.existsByImei(command.imei())) {
            throw new DeviceAlreadyExistsException("A device with the provided IMEI already exists.");
        }

        // 3. Creates a new device.
        Instant now = Instant.now();
        Device device = DeviceApplicationMapper.toDomain(command, now);

        // 4. Saves the device.
        Device savedDevice = devicePersistence.save(device);

        // 5. Builds the result.
        return DeviceApplicationMapper.toResult(savedDevice);
    }

    @Override
    public DeviceResult update(UpdateDeviceCommand command) {

        UUID id = command.id();

        // 1. Gets the device with the provided id.
        Device existingDevice = devicePersistence.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        // 2. Checks if exists another device with the given serial number.
        if (!existingDevice.getSerialNumber().equals(command.serialNumber())
                && devicePersistence.existsBySerialNumber(command.serialNumber())) {
            throw new DeviceAlreadyExistsException("A device with the provided serial number already exists.");
        }

        // 3. Checks if exists another device with the given IMEI.
        if (!existingDevice.getImei().equals(command.imei())
                && devicePersistence.existsByImei(command.imei())) {
            throw new DeviceAlreadyExistsException("A device with the provided IMEI already exists.");
        }

        // 4. Updates the device preserving identity and creation timestamp.
        Device deviceToSave = DeviceApplicationMapper.toDomain(command, existingDevice, Instant.now());

        // 5. Saves the device.
        Device updatedDevice = devicePersistence.save(deviceToSave);

        // 6. Builds the result.
        return DeviceApplicationMapper.toResult(updatedDevice);
    }

    /**
     * Retrieves a device by id.
     *
     * @param command get-by-id payload.
     * @return the matching device as a result object.
     */
    @Override
    public DeviceResult getById(GetDeviceByIdCommand command) {

        // 1. Resolves the target id directly from the inbound command.
        UUID id = command.id();
        UUID userId = command.userId();
        boolean isAdmin = command.isAdmin();

        // 2. Applies lookup strategy based on access scope (admin can fetch any device).
        Device device = (
                isAdmin
                ? devicePersistence.findById(id)
                : devicePersistence.findByIdAndOwnerId(id, userId)
        ).orElseThrow(() -> new DeviceNotFoundException(id));

        // 3. Maps the domain object to the response contract.
        return DeviceApplicationMapper.toResult(device);
    }

    /**
     * Retrieves a one-based page of devices.
     *
     * @param command page request payload.
     * @return paginated device result.
     */
    @Override
    public PageResult<DeviceResult> getPage(GetDevicePageCommand command) {

        // 1. Gets the params used to search.
        int pageNumber = command.pageNumber();
        int pageSize = command.pageSize();
        UUID userId = command.userId();

        // 2. Fetches the owner-scoped page from persistence preserving one-based indexing semantics.
        Page<Device> devicePageResult = devicePersistence.getPageByOwnerId(pageNumber, pageSize, userId);

        // 3. Converts domain page payload to application output contract.
        return DeviceApplicationMapper.toPageResult(devicePageResult);
    }

}

