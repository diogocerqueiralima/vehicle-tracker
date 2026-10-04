package com.github.diogocerqueiralima.asset.service.application.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.CreateOrUpdateDeviceCommand
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import org.springframework.data.domain.Page
import java.time.Instant

/**
 * Mapper for device conversions in the application layer.
 */
object DeviceApplicationMapper {

    /**
     * Builds a domain device from a create-or-update command, the pre-existing device with the same
     * id (if any), and the current timestamp. The device id always comes from the command, since the
     * client is responsible for supplying it.
     *
     * @param command create-or-update command with the device data.
     * @param createdAt creation timestamp of the existing device, or is equal to [now] when device doesn't exist.
     * @param now current timestamp; used as updatedAt always, and as createdAt when there is no existing device.
     * @return domain device with the provided data, preserving the original creation timestamp on update.
     */
    fun toDomain(command: CreateOrUpdateDeviceCommand, createdAt: Instant, now: Instant): Device =
        Device(
            command.id,
            command.ownerId,
            createdAt,
            now,
            command.serialNumber,
            command.model,
            command.manufacturer,
            command.imei
        )

    /**
     * Builds a device application result from a domain device.
     *
     * @param device domain device.
     * @return device application result.
     */
    fun toResult(device: Device): DeviceResult =
        DeviceResult(
            device.id,
            device.ownerId,
            device.createdAt,
            device.updatedAt,
            device.serialNumber,
            device.model,
            device.manufacturer,
            device.imei
        )

    /**
     * Converts a paginated domain payload into an application result payload.
     *
     * @param devicePageResult paginated domain devices.
     * @return paginated device application result.
     */
    fun toPageResult(devicePageResult: Page<Device>): PageResult<DeviceResult> =
        PageResult(
            devicePageResult.number + 1,
            devicePageResult.size,
            devicePageResult.totalPages,
            devicePageResult.totalElements,
            devicePageResult.map { toResult(it) }.toList()
        )

}
