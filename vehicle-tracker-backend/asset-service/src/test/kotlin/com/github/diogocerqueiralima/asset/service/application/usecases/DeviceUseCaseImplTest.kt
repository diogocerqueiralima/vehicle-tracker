package com.github.diogocerqueiralima.asset.service.application.usecases

import com.github.diogocerqueiralima.asset.service.application.commands.CreateOrUpdateDeviceCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetDevicePageCommand
import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.DevicePersistence
import com.github.diogocerqueiralima.error.common.exceptions.ConflictException
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class DeviceUseCaseImplTest {

    @Mock
    private lateinit var devicePersistence: DevicePersistence

    @InjectMocks
    private lateinit var deviceUseCase: DeviceUseCaseImpl

    @Test
    @DisplayName("Should create device with the client-supplied id when no device exists yet and serial number/IMEI are unique")
    fun should_create_device_when_no_device_exists_yet_and_serial_number_and_imei_are_unique() {
        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()

        val command = CreateOrUpdateDeviceCommand(
            id,
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345",
            ownerId
        )

        val now = Instant.parse("2026-03-15T12:00:00Z")
        val savedDevice = Device(
            id,
            now,
            now,
            command.serialNumber,
            command.model,
            command.manufacturer,
            command.imei
        )

        whenever(devicePersistence.findById(id)).thenReturn(null)
        whenever(devicePersistence.isSerialNumberOrImeiTakenByAnotherDevice(command.serialNumber, command.imei, id)).thenReturn(false)
        whenever(devicePersistence.save(any())).thenReturn(savedDevice)

        val result = deviceUseCase.createOrUpdate(command)

        assertThat(result.id).isEqualTo(id)
        assertThat(result.serialNumber).isEqualTo(command.serialNumber)
        assertThat(result.imei).isEqualTo(command.imei)
        verify(devicePersistence).save(any())
    }

    @Test
    @DisplayName("Should fail creating when no device exists yet but serial number or IMEI already exists")
    fun should_fail_creating_when_serial_number_or_imei_already_exists() {
        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()

        val command = CreateOrUpdateDeviceCommand(
            id,
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345",
            ownerId
        )

        whenever(devicePersistence.findById(id)).thenReturn(null)
        whenever(devicePersistence.isSerialNumberOrImeiTakenByAnotherDevice(command.serialNumber, command.imei, id)).thenReturn(true)

        assertThatThrownBy { deviceUseCase.createOrUpdate(command) }
            .isInstanceOf(ConflictException::class.java)

        verify(devicePersistence, never()).save(any())
    }

    @Test
    @DisplayName("Should update device when device exists and unique fields are valid")
    fun should_update_device_when_device_exists_and_unique_fields_are_valid() {

        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val createdAt = Instant.parse("2026-03-15T12:00:00Z")

        val existingDevice = Device(
            id,
            createdAt,
            createdAt,
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val command = CreateOrUpdateDeviceCommand(
            id,
            "SN-002",
            "TK-1100",
            "Teltonika",
            "223456789012345",
            ownerId
        )

        val updatedDevice = Device(
            id,
            createdAt,
            Instant.parse("2026-03-16T12:00:00Z"),
            command.serialNumber,
            command.model,
            command.manufacturer,
            command.imei
        )

        whenever(devicePersistence.findById(id)).thenReturn(existingDevice)
        whenever(devicePersistence.isSerialNumberOrImeiTakenByAnotherDevice(command.serialNumber, command.imei, id)).thenReturn(false)
        whenever(devicePersistence.save(any())).thenReturn(updatedDevice)

        val result = deviceUseCase.createOrUpdate(command)

        assertThat(result.id).isEqualTo(id)
        assertThat(result.createdAt).isEqualTo(createdAt)
        assertThat(result.serialNumber).isEqualTo("SN-002")
        assertThat(result.model).isEqualTo("TK-1100")
        verify(devicePersistence).save(any())
    }

    @Test
    @DisplayName("Should fail updating when serial number or IMEI already exists in another device")
    fun should_fail_updating_when_serial_number_or_imei_already_exists_in_another_device() {

        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val createdAt = Instant.parse("2026-03-15T12:00:00Z")

        val existingDevice = Device(
            id,
            createdAt,
            createdAt,
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val command = CreateOrUpdateDeviceCommand(
            id,
            "SN-002",
            "TK-1100",
            "Teltonika",
            "223456789012345",
            ownerId
        )

        whenever(devicePersistence.findById(id)).thenReturn(existingDevice)
        whenever(devicePersistence.isSerialNumberOrImeiTakenByAnotherDevice(command.serialNumber, command.imei, id)).thenReturn(true)

        assertThatThrownBy { deviceUseCase.createOrUpdate(command) }
            .isInstanceOf(ConflictException::class.java)

        verify(devicePersistence, never()).save(any())
    }

    @Test
    @DisplayName("Should get device by id when device exists")
    fun should_get_device_by_id_when_device_exists() {

        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val now = Instant.parse("2026-03-15T12:00:00Z")

        val device = Device(
            id,
            now,
            now,
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val command = GetDeviceByIdCommand(id, userId, false)

        whenever(devicePersistence.findByIdAndOwnerId(id, userId)).thenReturn(device)

        val result = deviceUseCase.getById(command)

        assertThat(result.id).isEqualTo(id)
        assertThat(result.serialNumber).isEqualTo(device.serialNumber)
        assertThat(result.imei).isEqualTo(device.imei)
        verify(devicePersistence).findByIdAndOwnerId(id, userId)
        verify(devicePersistence, never()).findById(id)
    }

    @Test
    @DisplayName("Should get device by id as admin when device exists")
    fun should_get_device_by_id_as_admin_when_device_exists() {

        val id = UUID.randomUUID()
        val adminUserId = UUID.randomUUID()
        val now = Instant.parse("2026-03-15T12:00:00Z")

        val device = Device(
            id,
            now,
            now,
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val command = GetDeviceByIdCommand(id, adminUserId, true)

        whenever(devicePersistence.findById(id)).thenReturn(device)

        val result = deviceUseCase.getById(command)

        assertThat(result.id).isEqualTo(id)
        assertThat(result.serialNumber).isEqualTo(device.serialNumber)
        assertThat(result.imei).isEqualTo(device.imei)
        verify(devicePersistence).findById(id)
        verify(devicePersistence, never()).findByIdAndOwnerId(id, adminUserId)
    }

    @Test
    @DisplayName("Should fail getting device by id when device does not exist")
    fun should_fail_getting_device_by_id_when_device_does_not_exist() {

        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val command = GetDeviceByIdCommand(id, userId, false)

        whenever(devicePersistence.findByIdAndOwnerId(id, userId)).thenReturn(null)

        assertThatThrownBy { deviceUseCase.getById(command) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Device not found for id: $id")
    }

    @Test
    @DisplayName("Should get device pageNumber when devices exist")
    fun should_get_device_page_when_devices_exist() {

        val pageNumber = 1
        val pageSize = 10
        val userId = UUID.randomUUID()

        val device = Device(
            UUID.randomUUID(),
            Instant.parse("2026-03-15T12:00:00Z"),
            Instant.parse("2026-03-15T12:00:00Z"),
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val devicePage = PageImpl(
            listOf(device),
            PageRequest.of(0, pageSize),
            1
        )

        val command = GetDevicePageCommand(pageNumber, pageSize, userId)

        whenever(devicePersistence.getPageByOwnerId(pageNumber - 1, pageSize, userId)).thenReturn(devicePage)

        val result = deviceUseCase.getPage(command)

        assertThat(result.pageNumber).isEqualTo(pageNumber)
        assertThat(result.pageSize).isEqualTo(pageSize)
        assertThat(result.totalElements).isEqualTo(1L)
        assertThat(result.totalPages).isEqualTo(1)
        assertThat(result.data).hasSize(1)
        assertThat(result.data.first().id).isEqualTo(device.id)

        verify(devicePersistence).getPageByOwnerId(pageNumber - 1, pageSize, userId)
    }

}
