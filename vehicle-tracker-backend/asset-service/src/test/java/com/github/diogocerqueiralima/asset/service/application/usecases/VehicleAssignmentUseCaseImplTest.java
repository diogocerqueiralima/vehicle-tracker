package com.github.diogocerqueiralima.asset.service.application.usecases;

import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException;
import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.DevicePersistence;
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehicleAssignmentPersistence;
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehiclePersistence;
import com.github.diogocerqueiralima.asset.service.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleAssignment;
import com.github.diogocerqueiralima.asset.service.domain.assignments.VehicleRemovalReason;
import com.github.diogocerqueiralima.asset.service.domain.assets.Device;
import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleAssignmentUseCaseImplTest {

    @Mock
    private DevicePersistence devicePersistence;

    @Mock
    private VehiclePersistence vehiclePersistence;

    @Mock
    private VehicleAssignmentPersistence vehicleAssignmentPersistence;

    @InjectMocks
    private VehicleAssignmentUseCaseImpl vehicleAssignmentUseCase;

    @Test
    @DisplayName("Should assign device to vehicle when both exist and are not active in other assignments")
    void should_assign_device_to_vehicle_when_both_exist_and_are_not_active_in_other_assignments() {

        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        UUID installedBy = UUID.randomUUID();

        Device device = new Device(
                deviceId,
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        Vehicle vehicle = new Vehicle(
                vehicleId,
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        AssignDeviceToVehicleCommand command = new AssignDeviceToVehicleCommand(
                deviceId,
                vehicleId,
                assignedBy,
                installedBy,
                "Installed in workshop A"
        );

        VehicleAssignment savedAssignment = new VehicleAssignment(
                device,
                vehicle,
                Instant.parse("2026-03-20T12:00:00Z"),
                null,
                assignedBy,
                null,
                null,
                installedBy,
                "Installed in workshop A"
        );

        when(devicePersistence.findByIdAndOwnerId(deviceId, assignedBy)).thenReturn(device);
        when(vehiclePersistence.findByIdAndOwnerId(vehicleId, assignedBy)).thenReturn(vehicle);
        when(vehicleAssignmentPersistence.save(any(VehicleAssignment.class))).thenReturn(savedAssignment);

        VehicleAssignmentResult result = vehicleAssignmentUseCase.assignDeviceToVehicle(command);

        assertThat(result.getDeviceId()).isEqualTo(deviceId);
        assertThat(result.getVehicleId()).isEqualTo(vehicleId);
        assertThat(result.getAssignedBy()).isEqualTo(assignedBy);
        assertThat(result.getInstalledBy()).isEqualTo(installedBy);
        assertThat(result.getActive()).isTrue();

        verify(vehicleAssignmentPersistence).save(any(VehicleAssignment.class));
    }

    @Test
    @DisplayName("Should fail assigning when device does not exist")
    void should_fail_assigning_when_device_does_not_exist() {

        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        AssignDeviceToVehicleCommand command = new AssignDeviceToVehicleCommand(
                deviceId,
                vehicleId,
                UUID.randomUUID(),
                null,
                null
        );

        when(devicePersistence.findByIdAndOwnerId(deviceId, command.getAssignedBy())).thenReturn(null);

        assertThatThrownBy(() -> vehicleAssignmentUseCase.assignDeviceToVehicle(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Device not found for id: " + deviceId);

        verify(vehicleAssignmentPersistence, never()).save(any(VehicleAssignment.class));
    }

    @Test
    @DisplayName("Should fail assigning when vehicle does not exist")
    void should_fail_assigning_when_vehicle_does_not_exist() {

        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        Device device = new Device(
                deviceId,
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        AssignDeviceToVehicleCommand command = new AssignDeviceToVehicleCommand(
                deviceId,
                vehicleId,
                UUID.randomUUID(),
                null,
                null
        );

        when(devicePersistence.findByIdAndOwnerId(deviceId, command.getAssignedBy())).thenReturn(device);
        when(vehiclePersistence.findByIdAndOwnerId(vehicleId, command.getAssignedBy())).thenReturn(null);

        assertThatThrownBy(() -> vehicleAssignmentUseCase.assignDeviceToVehicle(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Vehicle not found for id: " + vehicleId);

        verify(vehicleAssignmentPersistence, never()).save(any(VehicleAssignment.class));
    }

    @Test
    @DisplayName("Should unassign device from vehicle when active assignment exists")
    void should_unassign_device_from_vehicle_when_active_assignment_exists() {

        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        UUID unassignedBy = UUID.randomUUID();
        UUID installedBy = UUID.randomUUID();

        Device device = new Device(
                deviceId,
                unassignedBy,
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        Vehicle vehicle = new Vehicle(
                vehicleId,
                unassignedBy,
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                "1HGCM82633A123456",
                "AA-00-AA",
                "Model 3",
                "Tesla",
                LocalDate.of(2024, 1, 15)
        );

        VehicleAssignment activeAssignment = new VehicleAssignment(
                device,
                vehicle,
                Instant.parse("2026-03-20T12:00:00Z"),
                null,
                assignedBy,
                null,
                null,
                installedBy,
                "Installed in workshop A"
        );

        VehicleAssignment closedAssignment = new VehicleAssignment(
                device,
                vehicle,
                activeAssignment.getAssignedAt(),
                Instant.parse("2026-04-01T08:00:00Z"),
                assignedBy,
                unassignedBy,
                VehicleRemovalReason.RETIRED,
                installedBy,
                "Installed in workshop A"
        );

        UnassignDeviceFromVehicleCommand command = new UnassignDeviceFromVehicleCommand(
                deviceId,
                vehicleId,
                unassignedBy,
                VehicleRemovalReason.RETIRED
        );

        when(devicePersistence.isOwner(deviceId, command.getUnassignedBy())).thenReturn(true);
        when(vehiclePersistence.isOwner(vehicleId, command.getUnassignedBy())).thenReturn(true);
        when(vehicleAssignmentPersistence.findActiveByDeviceIdAndVehicleId(deviceId, vehicleId))
                .thenReturn(activeAssignment);
        when(vehicleAssignmentPersistence.save(any(VehicleAssignment.class))).thenReturn(closedAssignment);

        VehicleAssignmentResult result = vehicleAssignmentUseCase.unassignDeviceFromVehicle(command);

        assertThat(result.getDeviceId()).isEqualTo(deviceId);
        assertThat(result.getVehicleId()).isEqualTo(vehicleId);
        assertThat(result.getAssignedBy()).isEqualTo(assignedBy);
        assertThat(result.getInstalledBy()).isEqualTo(installedBy);
        assertThat(result.getActive()).isFalse();

        verify(vehicleAssignmentPersistence).save(any(VehicleAssignment.class));
    }

    @Test
    @DisplayName("Should fail unassigning when active assignment does not exist")
    void should_fail_unassigning_when_active_assignment_does_not_exist() {

        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        UnassignDeviceFromVehicleCommand command = new UnassignDeviceFromVehicleCommand(
                deviceId,
                vehicleId,
                UUID.randomUUID(),
                VehicleRemovalReason.OTHER
        );

        when(vehicleAssignmentPersistence.findActiveByDeviceIdAndVehicleId(deviceId, vehicleId))
                .thenReturn(null);

        assertThatThrownBy(() -> vehicleAssignmentUseCase.unassignDeviceFromVehicle(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Active vehicle assignment not found for device id: " + deviceId + " and vehicle id: " + vehicleId);

        verify(vehicleAssignmentPersistence, never()).save(any(VehicleAssignment.class));
    }

}