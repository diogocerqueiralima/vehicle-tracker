package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.application.commands.UpdateDeviceCommand;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.domain.assets.Device;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.UUID;

/**
 * Mapper for device conversions in the application layer.
 */
public final class DeviceApplicationMapper {

    // Should not be instantiated
    private DeviceApplicationMapper() {}

    /**
     *
     * Builds a domain device from a create command and the current timestamp.
     *
     * @param command create command with the device data.
     * @param now current timestamp for createdAt and updatedAt fields.
     * @return new domain device with the provided data and timestamps.
     */
    public static Device toDomain(CreateDeviceCommand command, Instant now) {
        return new Device(
                UUID.randomUUID(),
                now,
                now,
                command.serialNumber(),
                command.model(),
                command.manufacturer(),
                command.imei()
        );
    }

    /**
     *
     * Builds a domain device from an update command and the existing device.
     *
     * @param command update command with the new device data.
     * @param existingDevice existing device to be updated.
     * @param updatedAt timestamp of the update operation.
     * @return updated domain device with the new data and timestamps.
     */
    public static Device toDomain(UpdateDeviceCommand command, Device existingDevice, Instant updatedAt) {
        return new Device(
                existingDevice.getId(),
                existingDevice.getCreatedAt(),
                updatedAt,
                command.serialNumber(),
                command.model(),
                command.manufacturer(),
                command.imei()
        );
    }

    /**
     *
     * Builds a device application result from a domain device.
     *
     * @param device domain device.
     * @return device application result.
     */
    public static DeviceResult toResult(Device device) {
        return new DeviceResult(
                device.getId(),
                device.getCreatedAt(),
                device.getUpdatedAt(),
                device.getSerialNumber(),
                device.getModel(),
                device.getManufacturer(),
                device.getImei()
        );
    }

    /**
     * Converts a paginated domain payload into an application result payload.
     *
     * @param devicePageResult paginated domain devices.
     * @return paginated device application result.
     */
    public static PageResult<DeviceResult> toPageResult(Page<Device> devicePageResult) {
        return new PageResult<>(
                devicePageResult.getNumber() + 1,
                devicePageResult.getSize(),
                devicePageResult.getTotalElements(),
                devicePageResult.getTotalPages(),
                devicePageResult.stream()
                        .map(DeviceApplicationMapper::toResult)
                        .toList()
        );
    }

}

