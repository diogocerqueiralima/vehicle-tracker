package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.application.commands.UpdateDeviceCommand;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.domain.assets.Device;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceApplicationMapperTest {

    @Test
    @DisplayName("Should map create command to domain")
    void should_map_create_command_to_domain() {
        UUID ownerId = UUID.randomUUID();
        CreateDeviceCommand command = new CreateDeviceCommand(
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345",
                ownerId
        );

        Instant now = Instant.parse("2026-03-15T12:00:00Z");
        Device device = DeviceApplicationMapper.toDomain(command, now);

        assertThat(device.getId()).isNotNull();
        assertThat(device.getCreatedAt()).isEqualTo(now);
        assertThat(device.getUpdatedAt()).isEqualTo(now);
        assertThat(device.getSerialNumber()).isEqualTo(command.serialNumber());
        assertThat(device.getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("Should map update command to domain using existing device identity")
    void should_map_update_command_to_domain_using_existing_device_identity() {
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

        UpdateDeviceCommand command = new UpdateDeviceCommand(
                id,
                "SN-002",
                "TK-1100",
                "Teltonika",
                "223456789012345",
                ownerId
        );

        Instant updatedAt = Instant.parse("2026-03-20T10:00:00Z");
        Device mapped = DeviceApplicationMapper.toDomain(command, existingDevice, updatedAt);

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

