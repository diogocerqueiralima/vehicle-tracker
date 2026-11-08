package com.github.diogocerqueiralima.asset.service.application.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromVehicleCommand
import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleAssignment
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleRemovalReason
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class VehicleAssignmentApplicationMapperTest {

    private fun device(id: UUID) = Device(
        id,
        Instant.parse("2026-03-10T10:00:00Z"),
        Instant.parse("2026-03-10T10:00:00Z"),
        "SN-001",
        "TK-1000",
        "Teltonika",
        "123456789012345"
    )

    private fun vehicle(id: UUID) = Vehicle(
        id,
        Instant.parse("2026-03-10T10:00:00Z"),
        Instant.parse("2026-03-10T10:00:00Z"),
        "1HGCM82633A123456",
        "AA-00-AA",
        "Model 3",
        "Tesla",
        LocalDate.of(2024, 1, 15)
    )

    private fun activeAssignment(device: Device, vehicle: Vehicle, assignedBy: UUID) = VehicleAssignment(
        device,
        vehicle,
        Instant.parse("2026-03-20T12:00:00Z"),
        null,
        assignedBy,
        null,
        null,
        null,
        "Installed in workshop A"
    )

    @Test
    @DisplayName("Should map assign command to domain")
    fun should_map_assign_command_to_domain() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val installedBy = UUID.randomUUID()

        val command = AssignDeviceToVehicleCommand(
            deviceId,
            vehicleId,
            assignedBy,
            installedBy,
            "Installed in workshop A"
        )

        val assignedAt = Instant.parse("2026-03-20T12:00:00Z")
        val assignment = VehicleAssignmentApplicationMapper.toDomain(command, device(deviceId), vehicle(vehicleId), assignedAt)

        assertThat(assignment.device.id).isEqualTo(deviceId)
        assertThat(assignment.vehicle.id).isEqualTo(vehicleId)
        assertThat(assignment.assignedAt).isEqualTo(assignedAt)
        assertThat(assignment.assignedBy).isEqualTo(assignedBy)
        assertThat(assignment.installedBy).isEqualTo(installedBy)
        assertThat(assignment.notes).isEqualTo("Installed in workshop A")
        assertThat(assignment.isActive).isTrue()
    }

    @Test
    @DisplayName("Should map domain to result")
    fun should_map_domain_to_result() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()

        val assignment = activeAssignment(device(deviceId), vehicle(vehicleId), assignedBy)

        val result = VehicleAssignmentApplicationMapper.toResult(assignment)

        assertThat(result.deviceId).isEqualTo(deviceId)
        assertThat(result.vehicleId).isEqualTo(vehicleId)
        assertThat(result.assignedBy).isEqualTo(assignedBy)
        assertThat(result.unassignedAt).isNull()
        assertThat(result.unassignedBy).isNull()
        assertThat(result.removalReason).isNull()
        assertThat(result.notes).isEqualTo("Installed in workshop A")
        assertThat(result.active).isTrue()
    }

    @Test
    @DisplayName("Should map unassign command to inactive domain assignment")
    fun should_map_unassign_command_to_inactive_domain_assignment() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val unassignedBy = UUID.randomUUID()

        val activeAssignment = activeAssignment(device(deviceId), vehicle(vehicleId), assignedBy)

        val command = UnassignDeviceFromVehicleCommand(
            deviceId,
            vehicleId,
            unassignedBy,
            VehicleRemovalReason.OTHER
        )

        val unassignedAt = Instant.parse("2026-04-01T08:00:00Z")
        val assignment = VehicleAssignmentApplicationMapper.toDomain(command, activeAssignment, unassignedAt)

        assertThat(assignment.device.id).isEqualTo(deviceId)
        assertThat(assignment.vehicle.id).isEqualTo(vehicleId)
        assertThat(assignment.assignedBy).isEqualTo(assignedBy)
        assertThat(assignment.unassignedBy).isEqualTo(unassignedBy)
        assertThat(assignment.unassignedAt).isEqualTo(unassignedAt)
        assertThat(assignment.removalReason).isEqualTo(VehicleRemovalReason.OTHER)
        assertThat(assignment.isActive).isFalse()
    }

}
