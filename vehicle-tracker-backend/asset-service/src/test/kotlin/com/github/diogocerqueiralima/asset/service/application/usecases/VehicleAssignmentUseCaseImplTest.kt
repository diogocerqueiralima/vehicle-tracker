package com.github.diogocerqueiralima.asset.service.application.usecases

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromVehicleCommand
import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleAssignment
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleRemovalReason
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.DevicePersistence
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehicleAssignmentPersistence
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehiclePersistence
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
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class VehicleAssignmentUseCaseImplTest {

    @Mock
    private lateinit var devicePersistence: DevicePersistence

    @Mock
    private lateinit var vehiclePersistence: VehiclePersistence

    @Mock
    private lateinit var vehicleAssignmentPersistence: VehicleAssignmentPersistence

    @InjectMocks
    private lateinit var vehicleAssignmentUseCase: VehicleAssignmentUseCaseImpl

    @Test
    @DisplayName("Should assign device to vehicle when both exist and are not active in other assignments")
    fun should_assign_device_to_vehicle_when_both_exist_and_are_not_active_in_other_assignments() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val installedBy = UUID.randomUUID()

        val device = Device(
            deviceId,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val vehicle = Vehicle(
            vehicleId,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        val command = AssignDeviceToVehicleCommand(
            deviceId,
            vehicleId,
            assignedBy,
            installedBy,
            "Installed in workshop A"
        )

        val savedAssignment = VehicleAssignment(
            device,
            vehicle,
            Instant.parse("2026-03-20T12:00:00Z"),
            null,
            assignedBy,
            null,
            null,
            installedBy,
            "Installed in workshop A"
        )

        whenever(devicePersistence.findByIdAndOwnerId(deviceId, assignedBy)).thenReturn(device)
        whenever(vehiclePersistence.findByIdAndOwnerId(vehicleId, assignedBy)).thenReturn(vehicle)
        whenever(vehicleAssignmentPersistence.save(any())).thenReturn(savedAssignment)

        val result = vehicleAssignmentUseCase.assignDeviceToVehicle(command)

        assertThat(result.deviceId).isEqualTo(deviceId)
        assertThat(result.vehicleId).isEqualTo(vehicleId)
        assertThat(result.assignedBy).isEqualTo(assignedBy)
        assertThat(result.installedBy).isEqualTo(installedBy)
        assertThat(result.active).isTrue()

        verify(vehicleAssignmentPersistence).save(any())
    }

    @Test
    @DisplayName("Should fail assigning when device does not exist")
    fun should_fail_assigning_when_device_does_not_exist() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()

        val command = AssignDeviceToVehicleCommand(
            deviceId,
            vehicleId,
            UUID.randomUUID(),
            null,
            null
        )

        whenever(devicePersistence.findByIdAndOwnerId(deviceId, command.assignedBy)).thenReturn(null)

        assertThatThrownBy { vehicleAssignmentUseCase.assignDeviceToVehicle(command) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Device not found for id: $deviceId")

        verify(vehicleAssignmentPersistence, never()).save(any())
    }

    @Test
    @DisplayName("Should fail assigning when vehicle does not exist")
    fun should_fail_assigning_when_vehicle_does_not_exist() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()

        val device = Device(
            deviceId,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val command = AssignDeviceToVehicleCommand(
            deviceId,
            vehicleId,
            UUID.randomUUID(),
            null,
            null
        )

        whenever(devicePersistence.findByIdAndOwnerId(deviceId, command.assignedBy)).thenReturn(device)
        whenever(vehiclePersistence.findByIdAndOwnerId(vehicleId, command.assignedBy)).thenReturn(null)

        assertThatThrownBy { vehicleAssignmentUseCase.assignDeviceToVehicle(command) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Vehicle not found for id: $vehicleId")

        verify(vehicleAssignmentPersistence, never()).save(any())
    }

    @Test
    @DisplayName("Should unassign device from vehicle when active assignment exists")
    fun should_unassign_device_from_vehicle_when_active_assignment_exists() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val unassignedBy = UUID.randomUUID()
        val installedBy = UUID.randomUUID()

        val device = Device(
            deviceId,
            unassignedBy,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val vehicle = Vehicle(
            vehicleId,
            unassignedBy,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "1HGCM82633A123456",
            "AA-00-AA",
            "Model 3",
            "Tesla",
            LocalDate.of(2024, 1, 15)
        )

        val activeAssignment = VehicleAssignment(
            device,
            vehicle,
            Instant.parse("2026-03-20T12:00:00Z"),
            null,
            assignedBy,
            null,
            null,
            installedBy,
            "Installed in workshop A"
        )

        val closedAssignment = VehicleAssignment(
            device,
            vehicle,
            activeAssignment.assignedAt,
            Instant.parse("2026-04-01T08:00:00Z"),
            assignedBy,
            unassignedBy,
            VehicleRemovalReason.RETIRED,
            installedBy,
            "Installed in workshop A"
        )

        val command = UnassignDeviceFromVehicleCommand(
            deviceId,
            vehicleId,
            unassignedBy,
            VehicleRemovalReason.RETIRED
        )

        whenever(devicePersistence.isOwner(deviceId, command.unassignedBy)).thenReturn(true)
        whenever(vehiclePersistence.isOwner(vehicleId, command.unassignedBy)).thenReturn(true)
        whenever(vehicleAssignmentPersistence.findActiveByDeviceIdAndVehicleId(deviceId, vehicleId))
            .thenReturn(activeAssignment)
        whenever(vehicleAssignmentPersistence.save(any())).thenReturn(closedAssignment)

        val result = vehicleAssignmentUseCase.unassignDeviceFromVehicle(command)

        assertThat(result.deviceId).isEqualTo(deviceId)
        assertThat(result.vehicleId).isEqualTo(vehicleId)
        assertThat(result.assignedBy).isEqualTo(assignedBy)
        assertThat(result.installedBy).isEqualTo(installedBy)
        assertThat(result.active).isFalse()

        verify(vehicleAssignmentPersistence).save(any())
    }

    @Test
    @DisplayName("Should fail unassigning when active assignment does not exist")
    fun should_fail_unassigning_when_active_assignment_does_not_exist() {

        val deviceId = UUID.randomUUID()
        val vehicleId = UUID.randomUUID()

        val command = UnassignDeviceFromVehicleCommand(
            deviceId,
            vehicleId,
            UUID.randomUUID(),
            VehicleRemovalReason.OTHER
        )

        whenever(vehicleAssignmentPersistence.findActiveByDeviceIdAndVehicleId(deviceId, vehicleId))
            .thenReturn(null)

        assertThatThrownBy { vehicleAssignmentUseCase.unassignDeviceFromVehicle(command) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Active vehicle assignment not found for device id: $deviceId and vehicle id: $vehicleId")

        verify(vehicleAssignmentPersistence, never()).save(any())
    }

}
