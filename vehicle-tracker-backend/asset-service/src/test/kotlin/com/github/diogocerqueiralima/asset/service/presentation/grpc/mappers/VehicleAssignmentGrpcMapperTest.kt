package com.github.diogocerqueiralima.asset.service.presentation.grpc.mappers

import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult
import com.github.diogocerqueiralima.schema.proto.DeviceId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class VehicleAssignmentGrpcMapperTest {

    @Test
    @DisplayName("Should map DeviceId request to GetVehicleAssignmentByDeviceIdCommand")
    fun should_map_device_id_request_to_command() {

        val deviceId = UUID.randomUUID()

        val request = DeviceId.newBuilder()
            .setId(deviceId.toString())
            .build()

        val command = VehicleAssignmentGrpcMapper.toGetVehicleAssignmentByDeviceIdCommand(request)

        assertEquals(deviceId, command.deviceId)
    }

    @Test
    @DisplayName("Should map VehicleAssignmentResult to VehicleAssignmentResponse")
    fun should_map_vehicle_assignment_result_to_response() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val installedBy = UUID.randomUUID()
        val assignedAt = Instant.parse("2026-03-20T12:00:00Z")

        val result = VehicleAssignmentResult(
            deviceId,
            vehicleId,
            assignedAt,
            assignedBy,
            null,
            null,
            null,
            installedBy,
            "Installed in workshop A",
            true
        )

        val response = VehicleAssignmentGrpcMapper.toResponse(result)

        assertEquals(vehicleId.toString(), response.vehicleId)
        assertEquals(deviceId.toString(), response.deviceId)
        assertEquals(installedBy.toString(), response.installedBy)
        assertEquals(assignedAt.toEpochMilli(), response.assignedAt.seconds * 1000 + response.assignedAt.nanos / 1_000_000)
        assertEquals(assignedBy.toString(), response.assignedBy)
        assertFalse(response.hasUnassignedAt())
        assertFalse(response.hasUnassignedBy())
        assertFalse(response.hasRemovalReason())
        assertEquals(installedBy.toString(), response.installedBy)
        assertTrue(response.active)
        assertEquals(result.notes, response.notes)
    }

}
