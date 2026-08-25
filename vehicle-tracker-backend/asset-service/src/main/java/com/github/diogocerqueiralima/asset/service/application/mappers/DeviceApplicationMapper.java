package com.github.diogocerqueiralima.asset.service.application.mappers;

import com.github.diogocerqueiralima.asset.service.application.commands.CreateOrUpdateDeviceCommand;
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import com.github.diogocerqueiralima.asset.service.domain.assets.Device;
import org.springframework.data.domain.Page;

import java.time.Instant;

/**
 * Mapper for device conversions in the application layer.
 */
public final class DeviceApplicationMapper {

    // Should not be instantiated
    private DeviceApplicationMapper() {}

    /**
     *
     * Builds a domain device from a create-or-update command, the pre-existing device with the same
     * id (if any), and the current timestamp. The device id always comes from the command, since the
     * client is responsible for supplying it.
     *
     * @param command create-or-update command with the device data.
     * @param createdAt creation timestamp of the existing device, or is equal to {@code now} when device doesn't exist.
     * @param now current timestamp; used as updatedAt always, and as createdAt when there is no existing device.
     * @return domain device with the provided data, preserving the original creation timestamp on update.
     */
    public static Device toDomain(CreateOrUpdateDeviceCommand command, Instant createdAt, Instant now) {
        return new Device(
                command.id(),
                command.ownerId(),
                createdAt,
                now,
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
                device.getOwnerId(),
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
                devicePageResult.getTotalPages(),
                devicePageResult.getTotalElements(),
                devicePageResult.stream()
                        .map(DeviceApplicationMapper::toResult)
                        .toList()
        );
    }

}

