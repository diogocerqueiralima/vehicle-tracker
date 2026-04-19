package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.domain.assignments.VehicleAssignment;
import com.github.diogocerqueiralima.domain.assignments.VehicleRemovalReason;
import com.github.diogocerqueiralima.domain.assets.Device;
import com.github.diogocerqueiralima.domain.assets.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleAssignmentApplicationMapperTest {

    @Test
    @DisplayName("Should map assign command to domain")
    void should_map_assign_command_to_domain() {

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

        Instant assignedAt = Instant.parse("2026-03-20T12:00:00Z");
        VehicleAssignment assignment = VehicleAssignmentApplicationMapper.toDomain(command, device, vehicle, assignedAt);

        assertThat(assignment.getDevice().getId()).isEqualTo(deviceId);
        assertThat(assignment.getVehicle().getId()).isEqualTo(vehicleId);
        assertThat(assignment.getAssignedAt()).isEqualTo(assignedAt);
        assertThat(assignment.getAssignedBy()).isEqualTo(assignedBy);
        assertThat(assignment.getInstalledBy()).isEqualTo(installedBy);
        assertThat(assignment.getNotes()).isEqualTo("Installed in workshop A");
        assertThat(assignment.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map domain to result")
    void should_map_domain_to_result() {

        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();

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

        VehicleAssignment assignment = new VehicleAssignment(
                device,
                vehicle,
                Instant.parse("2026-03-20T12:00:00Z"),
                null,
                assignedBy,
                null,
                null,
                null,
                "Installed in workshop A"
        );

        VehicleAssignmentResult result = VehicleAssignmentApplicationMapper.toResult(assignment);

        assertThat(result.deviceId()).isEqualTo(deviceId);
        assertThat(result.vehicleId()).isEqualTo(vehicleId);
        assertThat(result.assignedBy()).isEqualTo(assignedBy);
        assertThat(result.unassignedAt()).isNull();
        assertThat(result.unassignedBy()).isNull();
        assertThat(result.removalReason()).isNull();
        assertThat(result.notes()).isEqualTo("Installed in workshop A");
        assertThat(result.active()).isTrue();
    }

    @Test
    @DisplayName("Should map unassign command to inactive domain assignment")
    void should_map_unassign_command_to_inactive_domain_assignment() {

        UUID deviceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        UUID unassignedBy = UUID.randomUUID();

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

        VehicleAssignment activeAssignment = new VehicleAssignment(
                device,
                vehicle,
                Instant.parse("2026-03-20T12:00:00Z"),
                null,
                assignedBy,
                null,
                null,
                null,
                "Installed in workshop A"
        );

        UnassignDeviceFromVehicleCommand command = new UnassignDeviceFromVehicleCommand(
                deviceId,
                vehicleId,
                unassignedBy,
                VehicleRemovalReason.OTHER
        );

        Instant unassignedAt = Instant.parse("2026-04-01T08:00:00Z");
        VehicleAssignment assignment = VehicleAssignmentApplicationMapper.toDomain(command, activeAssignment, unassignedAt);

        assertThat(assignment.getDevice().getId()).isEqualTo(deviceId);
        assertThat(assignment.getVehicle().getId()).isEqualTo(vehicleId);
        assertThat(assignment.getAssignedBy()).isEqualTo(assignedBy);
        assertThat(assignment.getUnassignedBy()).isEqualTo(unassignedBy);
        assertThat(assignment.getUnassignedAt()).isEqualTo(unassignedAt);
        assertThat(assignment.getRemovalReason()).isEqualTo(VehicleRemovalReason.OTHER);
        assertThat(assignment.isActive()).isFalse();
    }

}

