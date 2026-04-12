package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.application.commands.UpdateDeviceCommand;
import com.github.diogocerqueiralima.application.exceptions.DeviceAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.DeviceNotFoundException;
import com.github.diogocerqueiralima.application.ports.outbound.DevicePersistence;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.domain.assets.Device;
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
import java.util.Optional;
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
    @DisplayName("Should create device when serial number and IMEI are unique")
    void should_create_device_when_serial_number_and_imei_are_unique() {
        UUID ownerId = UUID.randomUUID();

        CreateDeviceCommand command = new CreateDeviceCommand(
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345",
                ownerId
        );

        Instant now = Instant.parse("2026-03-15T12:00:00Z");
        Device savedDevice = new Device(
                UUID.randomUUID(),
                now,
                now,
                command.serialNumber(),
                command.model(),
                command.manufacturer(),
                command.imei()
        );

        when(devicePersistence.existsBySerialNumber(command.serialNumber())).thenReturn(false);
        when(devicePersistence.existsByImei(command.imei())).thenReturn(false);
        when(devicePersistence.save(any(Device.class))).thenReturn(savedDevice);

        DeviceResult result = deviceUseCase.create(command);

        assertThat(result.id()).isEqualTo(savedDevice.getId());
        assertThat(result.serialNumber()).isEqualTo(command.serialNumber());
        assertThat(result.imei()).isEqualTo(command.imei());
        verify(devicePersistence).save(any(Device.class));
    }

    @Test
    @DisplayName("Should fail when serial number already exists")
    void should_fail_when_serial_number_already_exists() {
        UUID ownerId = UUID.randomUUID();

        CreateDeviceCommand command = new CreateDeviceCommand(
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345",
                ownerId
        );

        when(devicePersistence.existsBySerialNumber(command.serialNumber())).thenReturn(true);

        assertThatThrownBy(() -> deviceUseCase.create(command))
                .isInstanceOf(DeviceAlreadyExistsException.class)
                .hasMessage("A device with the provided serial number already exists.");

        verify(devicePersistence, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("Should fail when IMEI already exists")
    void should_fail_when_imei_already_exists() {
        UUID ownerId = UUID.randomUUID();

        CreateDeviceCommand command = new CreateDeviceCommand(
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345",
                ownerId
        );

        when(devicePersistence.existsBySerialNumber(command.serialNumber())).thenReturn(false);
        when(devicePersistence.existsByImei(command.imei())).thenReturn(true);

        assertThatThrownBy(() -> deviceUseCase.create(command))
                .isInstanceOf(DeviceAlreadyExistsException.class)
                .hasMessage("A device with the provided IMEI already exists.");

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

        UpdateDeviceCommand command = new UpdateDeviceCommand(
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
                command.serialNumber(),
                command.model(),
                command.manufacturer(),
                command.imei()
        );

        when(devicePersistence.findById(id)).thenReturn(Optional.of(existingDevice));
        when(devicePersistence.existsBySerialNumber(command.serialNumber())).thenReturn(false);
        when(devicePersistence.existsByImei(command.imei())).thenReturn(false);
        when(devicePersistence.save(any(Device.class))).thenReturn(updatedDevice);

        DeviceResult result = deviceUseCase.update(command);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.serialNumber()).isEqualTo("SN-002");
        assertThat(result.model()).isEqualTo("TK-1100");
        verify(devicePersistence).save(any(Device.class));
    }

    @Test
    @DisplayName("Should fail updating when device does not exist")
    void should_fail_updating_when_device_does_not_exist() {

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UpdateDeviceCommand command = new UpdateDeviceCommand(
                id,
                "SN-002",
                "TK-1100",
                "Teltonika",
                "223456789012345",
                ownerId
        );

        when(devicePersistence.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceUseCase.update(command))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessage("Device not found for id: " + id);

        verify(devicePersistence, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("Should fail updating when serial number already exists in another device")
    void should_fail_updating_when_serial_number_already_exists_in_another_device() {

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

        UpdateDeviceCommand command = new UpdateDeviceCommand(
                id,
                "SN-002",
                "TK-1100",
                "Teltonika",
                "123456789012345",
                ownerId
        );

        when(devicePersistence.findById(id)).thenReturn(Optional.of(existingDevice));
        when(devicePersistence.existsBySerialNumber(command.serialNumber())).thenReturn(true);

        assertThatThrownBy(() -> deviceUseCase.update(command))
                .isInstanceOf(DeviceAlreadyExistsException.class)
                .hasMessage("A device with the provided serial number already exists.");

        verify(devicePersistence, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("Should fail updating when IMEI already exists in another device")
    void should_fail_updating_when_imei_already_exists_in_another_device() {

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

        UpdateDeviceCommand command = new UpdateDeviceCommand(
                id,
                "SN-001",
                "TK-1100",
                "Teltonika",
                "223456789012345",
                ownerId
        );

        when(devicePersistence.findById(id)).thenReturn(Optional.of(existingDevice));
        when(devicePersistence.existsByImei(command.imei())).thenReturn(true);

        assertThatThrownBy(() -> deviceUseCase.update(command))
                .isInstanceOf(DeviceAlreadyExistsException.class)
                .hasMessage("A device with the provided IMEI already exists.");

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

        GetDeviceByIdCommand command = new GetDeviceByIdCommand(id, userId);

        when(devicePersistence.findByIdAndOwnerId(id, userId)).thenReturn(Optional.of(device));

        DeviceResult result = deviceUseCase.getById(command);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.serialNumber()).isEqualTo(device.getSerialNumber());
        assertThat(result.imei()).isEqualTo(device.getImei());
    }

    @Test
    @DisplayName("Should fail getting device by id when device does not exist")
    void should_fail_getting_device_by_id_when_device_does_not_exist() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GetDeviceByIdCommand command = new GetDeviceByIdCommand(id, userId);

        when(devicePersistence.findByIdAndOwnerId(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceUseCase.getById(command))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessage("Device not found for id: " + id);
    }

    @Test
    @DisplayName("Should get device page when devices exist")
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

        when(devicePersistence.getPageByOwnerId(pageNumber, pageSize, userId)).thenReturn(devicePage);

        PageResult<DeviceResult> result = deviceUseCase.getPage(command);

        assertThat(result.pageNumber()).isEqualTo(pageNumber);
        assertThat(result.pageSize()).isEqualTo(pageSize);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().getFirst().id()).isEqualTo(device.getId());

        verify(devicePersistence).getPageByOwnerId(pageNumber, pageSize, userId);
    }

}

