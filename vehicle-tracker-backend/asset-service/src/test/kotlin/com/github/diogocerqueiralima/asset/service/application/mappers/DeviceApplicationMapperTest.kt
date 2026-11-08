package com.github.diogocerqueiralima.asset.service.application.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.CreateOrUpdateDeviceCommand
import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class DeviceApplicationMapperTest {

    @Test
    @DisplayName("Should map create-or-update command to domain using the client-supplied id when no device exists yet")
    fun should_map_command_to_domain_using_client_supplied_id_when_no_device_exists_yet() {
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
        val device = DeviceApplicationMapper.toDomain(command, now, now)

        assertThat(device.id).isEqualTo(id)
        assertThat(device.createdAt).isEqualTo(now)
        assertThat(device.updatedAt).isEqualTo(now)
        assertThat(device.serialNumber).isEqualTo(command.serialNumber)
        assertThat(device.ownerId).isEqualTo(ownerId)
    }

    @Test
    @DisplayName("Should map create-or-update command to domain preserving the existing device's creation timestamp")
    fun should_map_command_to_domain_preserving_existing_device_creation_timestamp() {
        val id = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val createdAt = Instant.parse("2026-03-10T12:00:00Z")
        val existingDevice = Device(
            id,
            ownerId,
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

        val updatedAt = Instant.parse("2026-03-20T10:00:00Z")
        val mapped = DeviceApplicationMapper.toDomain(command, existingDevice.createdAt, updatedAt)

        assertThat(mapped.id).isEqualTo(id)
        assertThat(mapped.createdAt).isEqualTo(createdAt)
        assertThat(mapped.updatedAt).isEqualTo(updatedAt)
        assertThat(mapped.serialNumber).isEqualTo("SN-002")
        assertThat(mapped.ownerId).isEqualTo(ownerId)
    }

    @Test
    @DisplayName("Should map domain to result")
    fun should_map_domain_to_result() {
        val id = UUID.randomUUID()
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

        val result = DeviceApplicationMapper.toResult(device)

        assertThat(result.id).isEqualTo(id)
        assertThat(result.serialNumber).isEqualTo(device.serialNumber)
        assertThat(result.imei).isEqualTo(device.imei)
    }

}
