package com.github.diogocerqueiralima.asset.service.application.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromSimCardCommand
import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard
import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardAssignment
import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardRemovalReason
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class SimCardAssignmentApplicationMapperTest {

    private fun device(id: UUID) = Device(
        id,
        Instant.parse("2026-03-10T10:00:00Z"),
        Instant.parse("2026-03-10T10:00:00Z"),
        "SN-001",
        "TK-1000",
        "Teltonika",
        "123456789012345"
    )

    private fun simCard(id: UUID, iccid: String, msisdn: String, imsi: String) = SimCard(
        id,
        Instant.parse("2026-03-10T10:00:00Z"),
        Instant.parse("2026-03-10T10:00:00Z"),
        iccid,
        msisdn,
        imsi
    )

    private fun activeAssignment(device: Device, simCard: SimCard, assignedBy: UUID) = SimCardAssignment(
        device,
        simCard,
        Instant.parse("2026-03-20T12:00:00Z"),
        null,
        assignedBy,
        null,
        null
    )

    @Test
    @DisplayName("Should map assign command to domain")
    fun should_map_assign_command_to_domain() {

        val deviceId = UUID.randomUUID()
        val simCardId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()

        val simCard = simCard(simCardId, "8991101200003204510", "+351910000000", "268011234567890")
        val command = AssignDeviceToSimCardCommand(deviceId, simCardId, assignedBy)

        val assignedAt = Instant.parse("2026-03-20T12:00:00Z")
        val assignment = SimCardAssignmentApplicationMapper.toDomain(
            command,
            device(deviceId),
            simCard,
            assignedAt
        )

        assertThat(assignment.device.id).isEqualTo(deviceId)
        assertThat(assignment.simCard.id).isEqualTo(simCardId)
        assertThat(assignment.assignedAt).isEqualTo(assignedAt)
        assertThat(assignment.assignedBy).isEqualTo(assignedBy)
        assertThat(assignment.isActive).isTrue()
    }

    @Test
    @DisplayName("Should map domain to result")
    fun should_map_domain_to_result() {

        val deviceId = UUID.randomUUID()
        val simCardId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()

        val assignment = activeAssignment(
            device(deviceId),
            simCard(simCardId, "8991101200003204511", "+351910000001", "268011234567891"),
            assignedBy
        )

        val result = SimCardAssignmentApplicationMapper.toResult(assignment)

        assertThat(result.deviceId).isEqualTo(deviceId)
        assertThat(result.simCardId).isEqualTo(simCardId)
        assertThat(result.assignedBy).isEqualTo(assignedBy)
        assertThat(result.unassignedAt).isNull()
        assertThat(result.unassignedBy).isNull()
        assertThat(result.removalReason).isNull()
        assertThat(result.active).isTrue()
    }

    @Test
    @DisplayName("Should map unassign command to inactive domain assignment")
    fun should_map_unassign_command_to_inactive_domain_assignment() {

        val deviceId = UUID.randomUUID()
        val simCardId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val unassignedBy = UUID.randomUUID()

        val activeAssignment = activeAssignment(
            device(deviceId),
            simCard(simCardId, "8991101200003204512", "+351910000002", "268011234567892"),
            assignedBy
        )

        val command = UnassignDeviceFromSimCardCommand(
            deviceId,
            simCardId,
            unassignedBy,
            SimCardRemovalReason.DAMAGE
        )

        val unassignedAt = Instant.parse("2026-04-01T08:00:00Z")
        val assignment = SimCardAssignmentApplicationMapper.toDomain(
            command,
            activeAssignment,
            unassignedAt
        )

        assertThat(assignment.device.id).isEqualTo(deviceId)
        assertThat(assignment.simCard.id).isEqualTo(simCardId)
        assertThat(assignment.assignedBy).isEqualTo(assignedBy)
        assertThat(assignment.unassignedBy).isEqualTo(unassignedBy)
        assertThat(assignment.unassignedAt).isEqualTo(unassignedAt)
        assertThat(assignment.removalReason).isEqualTo(SimCardRemovalReason.DAMAGE)
        assertThat(assignment.isActive).isFalse()
    }

}
