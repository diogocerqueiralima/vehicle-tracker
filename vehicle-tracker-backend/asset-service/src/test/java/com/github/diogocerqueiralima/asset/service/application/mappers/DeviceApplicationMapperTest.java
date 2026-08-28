package com.github.diogocerqueiralima.asset.service.application.mappers;

import com.github.diogocerqueiralima.asset.service.application.commands.CreateOrUpdateDeviceCommand;
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult;
import com.github.diogocerqueiralima.asset.service.domain.assets.Device;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceApplicationMapperTest {

    @Test
    @DisplayName("Should map create-or-update command to domain using the client-supplied id when no device exists yet")
    void should_map_command_to_domain_using_client_supplied_id_when_no_device_exists_yet() {
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
        Device device = DeviceApplicationMapper.toDomain(command, now, now);

        assertThat(device.getId()).isEqualTo(id);
        assertThat(device.getCreatedAt()).isEqualTo(now);
        assertThat(device.getUpdatedAt()).isEqualTo(now);
        assertThat(device.getSerialNumber()).isEqualTo(command.serialNumber());
        assertThat(device.getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("Should map create-or-update command to domain preserving the existing device's creation timestamp")
    void should_map_command_to_domain_preserving_existing_device_creation_timestamp() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-10T12:00:00Z");
        Device existingDevice = new Device(
                id,
                ownerId,
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

        Instant updatedAt = Instant.parse("2026-03-20T10:00:00Z");
        Device mapped = DeviceApplicationMapper.toDomain(command, existingDevice.getCreatedAt(), updatedAt);

        assertThat(mapped.getId()).isEqualTo(id);
        assertThat(mapped.getCreatedAt()).isEqualTo(createdAt);
        assertThat(mapped.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(mapped.getSerialNumber()).isEqualTo("SN-002");
        assertThat(mapped.getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("Should map domain to result")
    void should_map_domain_to_result() {
        UUID id = UUID.randomUUID();
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

        DeviceResult result = DeviceApplicationMapper.toResult(device);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.serialNumber()).isEqualTo(device.getSerialNumber());
        assertThat(result.imei()).isEqualTo(device.getImei());
    }

}

