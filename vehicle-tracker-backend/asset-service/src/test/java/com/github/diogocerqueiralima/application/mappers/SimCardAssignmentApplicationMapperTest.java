package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardAssignmentResult;
import com.github.diogocerqueiralima.domain.assets.SimCard;
import com.github.diogocerqueiralima.domain.assignments.SimCardAssignment;
import com.github.diogocerqueiralima.domain.assignments.SimCardRemovalReason;
import com.github.diogocerqueiralima.domain.assets.Device;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimCardAssignmentApplicationMapperTest {

    @Test
    @DisplayName("Should map assign command to domain")
    void should_map_assign_command_to_domain() {

        UUID deviceId = UUID.randomUUID();
        UUID simCardId = UUID.randomUUID();
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

        SimCard simCard = new SimCard(
                simCardId,
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                "8991101200003204510",
                "+351910000000",
                "268011234567890"
        );

        AssignDeviceToSimCardCommand command = new AssignDeviceToSimCardCommand(
                deviceId,
                simCardId,
                assignedBy
        );

        Instant assignedAt = Instant.parse("2026-03-20T12:00:00Z");
        SimCardAssignment assignment = SimCardAssignmentApplicationMapper.toDomain(
                command,
                device,
                simCard,
                assignedAt
        );

        assertThat(assignment.getDevice().getId()).isEqualTo(deviceId);
        assertThat(assignment.getSimCard().getId()).isEqualTo(simCardId);
        assertThat(assignment.getAssignedAt()).isEqualTo(assignedAt);
        assertThat(assignment.getAssignedBy()).isEqualTo(assignedBy);
        assertThat(assignment.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map domain to result")
    void should_map_domain_to_result() {

        UUID deviceId = UUID.randomUUID();
        UUID simCardId = UUID.randomUUID();
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

        SimCard simCard = new SimCard(
                simCardId,
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                "8991101200003204511",
                "+351910000001",
                "268011234567891"
        );

        SimCardAssignment assignment = new SimCardAssignment(
                device,
                simCard,
                Instant.parse("2026-03-20T12:00:00Z"),
                null,
                assignedBy,
                null,
                null
        );

        SimCardAssignmentResult result = SimCardAssignmentApplicationMapper.toResult(assignment);

        assertThat(result.deviceId()).isEqualTo(deviceId);
        assertThat(result.simCardId()).isEqualTo(simCardId);
        assertThat(result.assignedBy()).isEqualTo(assignedBy);
        assertThat(result.unassignedAt()).isNull();
        assertThat(result.unassignedBy()).isNull();
        assertThat(result.removalReason()).isNull();
        assertThat(result.active()).isTrue();
    }

    @Test
    @DisplayName("Should map unassign command to inactive domain assignment")
    void should_map_unassign_command_to_inactive_domain_assignment() {

        UUID deviceId = UUID.randomUUID();
        UUID simCardId = UUID.randomUUID();
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

        SimCard simCard = new SimCard(
                simCardId,
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                "8991101200003204512",
                "+351910000002",
                "268011234567892"
        );

        SimCardAssignment activeAssignment = new SimCardAssignment(
                device,
                simCard,
                Instant.parse("2026-03-20T12:00:00Z"),
                null,
                assignedBy,
                null,
                null
        );

        UnassignDeviceFromSimCardCommand command = new UnassignDeviceFromSimCardCommand(
                deviceId,
                simCardId,
                unassignedBy,
                SimCardRemovalReason.DAMAGE
        );

        Instant unassignedAt = Instant.parse("2026-04-01T08:00:00Z");
        SimCardAssignment assignment = SimCardAssignmentApplicationMapper.toDomain(
                command,
                activeAssignment,
                unassignedAt
        );

        assertThat(assignment.getDevice().getId()).isEqualTo(deviceId);
        assertThat(assignment.getSimCard().getId()).isEqualTo(simCardId);
        assertThat(assignment.getAssignedBy()).isEqualTo(assignedBy);
        assertThat(assignment.getUnassignedBy()).isEqualTo(unassignedBy);
        assertThat(assignment.getUnassignedAt()).isEqualTo(unassignedAt);
        assertThat(assignment.getRemovalReason()).isEqualTo(SimCardRemovalReason.DAMAGE);
        assertThat(assignment.isActive()).isFalse();
    }

}

