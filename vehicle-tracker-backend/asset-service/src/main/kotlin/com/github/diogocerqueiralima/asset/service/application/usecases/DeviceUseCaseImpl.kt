package com.github.diogocerqueiralima.asset.service.application.usecases

import com.github.diogocerqueiralima.error.common.exceptions.ConflictException
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.asset.service.application.commands.CreateOrUpdateDeviceCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetDevicePageCommand
import com.github.diogocerqueiralima.asset.service.application.mappers.DeviceApplicationMapper
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.DeviceUseCase
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.DevicePersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Application-layer implementation that orchestrates device use cases.
 */
@Service
class DeviceUseCaseImpl(private val devicePersistence: DevicePersistence) : DeviceUseCase {

    @Transactional
    override fun createOrUpdate(command: CreateOrUpdateDeviceCommand): DeviceResult {

        val id = command.id

        // 1. Looks up an existing device with the client-supplied id: present means update, absent means create.
        val existingDevice = devicePersistence.findById(id)

        // 2. Fails when the serial number or IMEI is already used by a different device.
        //    Excluding id is a no-op when the device does not exist yet, so this covers create and update alike.
        if (devicePersistence.isSerialNumberOrImeiTakenByAnotherDevice(command.serialNumber, command.imei, id)) {
            throw ConflictException("A device with the provided serial number or IMEI already exists.")
        }

        // 3. Builds the device, preserving the original creation timestamp when updating.
        val now = Instant.now()
        val deviceToSave = DeviceApplicationMapper.toDomain(command, existingDevice?.createdAt ?: now, now)

        // 4. Saves the device.
        val savedDevice = devicePersistence.save(deviceToSave)

        // 5. Builds the result.
        return DeviceApplicationMapper.toResult(savedDevice)
    }

    /**
     * Retrieves a device by id.
     *
     * @param command get-by-id payload.
     * @return the matching device as a result object.
     */
    override fun getById(command: GetDeviceByIdCommand): DeviceResult {

        // 1. Resolves the target id directly from the inbound command.
        val id = command.id
        val userId = command.userId

        // 2. Applies lookup strategy based on access scope (admin can fetch any device).
        val device = (
            if (command.isAdmin) devicePersistence.findById(id)
            else devicePersistence.findByIdAndOwnerId(id, userId)
        ) ?: throw NotFoundException("Device not found for id: $id")

        // 3. Maps the domain object to the response contract.
        return DeviceApplicationMapper.toResult(device)
    }

    /**
     * Retrieves a one-based page of devices.
     *
     * @param command page request payload.
     * @return paginated device result.
     */
    override fun getPage(command: GetDevicePageCommand): PageResult<DeviceResult> {

        // 1. Fetches the owner-scoped page from persistence preserving one-based indexing semantics.
        val devicePageResult = devicePersistence.getPageByOwnerId(command.pageNumber - 1, command.pageSize, command.userId)

        // 2. Converts domain page payload to application output contract.
        return DeviceApplicationMapper.toPageResult(devicePageResult)
    }

}
