package com.github.diogocerqueiralima.asset.service.application.usecases;

import com.github.diogocerqueiralima.error.common.exceptions.ConflictException;
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException;
import com.github.diogocerqueiralima.asset.service.application.commands.CreateOrUpdateDeviceCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.DevicePersistence;
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import com.github.diogocerqueiralima.asset.service.domain.assets.Device;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceUseCaseImplTest {

    @Mock
    private DevicePersistence devicePersistence;

    @InjectMocks
    private DeviceUseCaseImpl deviceUseCase;

    @Test
    @DisplayName("Should create device with the client-supplied id when no device exists yet and serial number/IMEI are unique")
    void should_create_device_when_no_device_exists_yet_and_serial_number_and_imei_are_unique() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        CreateOrUpdateDeviceCommand command = new CreateOrUpdateDeviceCommand(
                id,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345",
                ownerId
        );

        Instant now = Instant.parse("2026-03-15T12:00:00Z");
        Device savedDevice = new Device(
                id,
                now,
                now,
                command.getSerialNumber(),
                command.getModel(),
                command.getManufacturer(),
                command.getImei()
        );

        when(devicePersistence.findById(id)).thenReturn(null);
        when(devicePersistence.isSerialNumberOrImeiTakenByAnotherDevice(command.getSerialNumber(), command.getImei(), id)).thenReturn(false);
        when(devicePersistence.save(any(Device.class))).thenReturn(savedDevice);

        DeviceResult result = deviceUseCase.createOrUpdate(command);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getSerialNumber()).isEqualTo(command.getSerialNumber());
        assertThat(result.getImei()).isEqualTo(command.getImei());
        verify(devicePersistence).save(any(Device.class));
    }

    @Test
    @DisplayName("Should fail creating when no device exists yet but serial number or IMEI already exists")
    void should_fail_creating_when_serial_number_or_imei_already_exists() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        CreateOrUpdateDeviceCommand command = new CreateOrUpdateDeviceCommand(
                id,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345",
                ownerId
        );

        when(devicePersistence.findById(id)).thenReturn(null);
        when(devicePersistence.isSerialNumberOrImeiTakenByAnotherDevice(command.getSerialNumber(), command.getImei(), id)).thenReturn(true);

        assertThatThrownBy(() -> deviceUseCase.createOrUpdate(command))
                .isInstanceOf(ConflictException.class);

        verify(devicePersistence, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("Should update device when device exists and unique fields are valid")
    void should_update_device_when_device_exists_and_unique_fields_are_valid() {

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-15T12:00:00Z");

        Device existingDevice = new Device(
                id,
                createdAt,
                createdAt,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        CreateOrUpdateDeviceCommand command = new CreateOrUpdateDeviceCommand(
                id,
                "SN-002",
                "TK-1100",
                "Teltonika",
                "223456789012345",
                ownerId
        );

        Device updatedDevice = new Device(
                id,
                createdAt,
                Instant.parse("2026-03-16T12:00:00Z"),
                command.getSerialNumber(),
                command.getModel(),
                command.getManufacturer(),
                command.getImei()
        );

        when(devicePersistence.findById(id)).thenReturn(existingDevice);
        when(devicePersistence.isSerialNumberOrImeiTakenByAnotherDevice(command.getSerialNumber(), command.getImei(), id)).thenReturn(false);
        when(devicePersistence.save(any(Device.class))).thenReturn(updatedDevice);

        DeviceResult result = deviceUseCase.createOrUpdate(command);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getSerialNumber()).isEqualTo("SN-002");
        assertThat(result.getModel()).isEqualTo("TK-1100");
        verify(devicePersistence).save(any(Device.class));
    }

    @Test
    @DisplayName("Should fail updating when serial number or IMEI already exists in another device")
    void should_fail_updating_when_serial_number_or_imei_already_exists_in_another_device() {

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-15T12:00:00Z");

        Device existingDevice = new Device(
                id,
                createdAt,
                createdAt,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        CreateOrUpdateDeviceCommand command = new CreateOrUpdateDeviceCommand(
                id,
                "SN-002",
                "TK-1100",
                "Teltonika",
                "223456789012345",
                ownerId
        );

        when(devicePersistence.findById(id)).thenReturn(existingDevice);
        when(devicePersistence.isSerialNumberOrImeiTakenByAnotherDevice(command.getSerialNumber(), command.getImei(), id)).thenReturn(true);

        assertThatThrownBy(() -> deviceUseCase.createOrUpdate(command))
                .isInstanceOf(ConflictException.class);

        verify(devicePersistence, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("Should get device by id when device exists")
    void should_get_device_by_id_when_device_exists() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        Device device = new Device(
                id,
                now,
                now,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        GetDeviceByIdCommand command = new GetDeviceByIdCommand(id, userId, false);

        when(devicePersistence.findByIdAndOwnerId(id, userId)).thenReturn(device);

        DeviceResult result = deviceUseCase.getById(command);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getSerialNumber()).isEqualTo(device.getSerialNumber());
        assertThat(result.getImei()).isEqualTo(device.getImei());
        verify(devicePersistence).findByIdAndOwnerId(id, userId);
        verify(devicePersistence, never()).findById(id);
    }

    @Test
    @DisplayName("Should get device by id as admin when device exists")
    void should_get_device_by_id_as_admin_when_device_exists() {

        UUID id = UUID.randomUUID();
        UUID adminUserId = UUID.randomUUID();
        Instant now = Instant.parse("2026-03-15T12:00:00Z");

        Device device = new Device(
                id,
                now,
                now,
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        GetDeviceByIdCommand command = new GetDeviceByIdCommand(id, adminUserId, true);

        when(devicePersistence.findById(id)).thenReturn(device);

        DeviceResult result = deviceUseCase.getById(command);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getSerialNumber()).isEqualTo(device.getSerialNumber());
        assertThat(result.getImei()).isEqualTo(device.getImei());
        verify(devicePersistence).findById(id);
        verify(devicePersistence, never()).findByIdAndOwnerId(id, adminUserId);
    }

    @Test
    @DisplayName("Should fail getting device by id when device does not exist")
    void should_fail_getting_device_by_id_when_device_does_not_exist() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GetDeviceByIdCommand command = new GetDeviceByIdCommand(id, userId, false);

        when(devicePersistence.findByIdAndOwnerId(id, userId)).thenReturn(null);

        assertThatThrownBy(() -> deviceUseCase.getById(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Device not found for id: " + id);
    }

    @Test
    @DisplayName("Should get device pageNumber when devices exist")
    void should_get_device_page_when_devices_exist() {

        int pageNumber = 1;
        int pageSize = 10;
        UUID userId = UUID.randomUUID();

        Device device = new Device(
                UUID.randomUUID(),
                Instant.parse("2026-03-15T12:00:00Z"),
                Instant.parse("2026-03-15T12:00:00Z"),
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        Page<Device> devicePage = new PageImpl<>(
                List.of(device),
                PageRequest.of(0, pageSize),
                1
        );

        GetDevicePageCommand command = new GetDevicePageCommand(pageNumber, pageSize, userId);

        when(devicePersistence.getPageByOwnerId(pageNumber - 1, pageSize, userId)).thenReturn(devicePage);

        PageResult<DeviceResult> result = deviceUseCase.getPage(command);

        assertThat(result.getPageNumber()).isEqualTo(pageNumber);
        assertThat(result.getPageSize()).isEqualTo(pageSize);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst().getId()).isEqualTo(device.getId());

        verify(devicePersistence).getPageByOwnerId(pageNumber - 1, pageSize, userId);
    }

}
