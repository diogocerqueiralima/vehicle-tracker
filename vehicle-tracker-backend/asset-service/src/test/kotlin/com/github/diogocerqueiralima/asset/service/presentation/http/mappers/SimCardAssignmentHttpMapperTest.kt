package com.github.diogocerqueiralima.asset.service.presentation.http.mappers

import com.github.diogocerqueiralima.asset.service.application.results.SimCardAssignmentResult
import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardRemovalReason
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.AssignDeviceToSimCardRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UnassignDeviceFromSimCardRequestDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class SimCardAssignmentHttpMapperTest {

    @Test
    @DisplayName("Should map assign request to command")
    fun should_map_assign_request_to_command() {

        val deviceId = UUID.randomUUID()
        val simCardId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()

        val request = AssignDeviceToSimCardRequestDTO(deviceId)

        val command = SimCardAssignmentHttpMapper.toAssignDeviceToSimCardCommand(
            request,
            simCardId,
            assignedBy
        )

        assertThat(command.deviceId).isEqualTo(deviceId)
        assertThat(command.simCardId).isEqualTo(simCardId)
        assertThat(command.assignedBy).isEqualTo(assignedBy)
    }

    @Test
    @DisplayName("Should map assignment result to dto")
    fun should_map_assignment_result_to_dto() {

        val deviceId = UUID.randomUUID()
        val simCardId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val assignedAt = Instant.parse("2026-03-20T12:00:00Z")

        val result = SimCardAssignmentResult(
            deviceId,
            simCardId,
            assignedAt,
            assignedBy,
            null,
            null,
            null,
            true
        )

        val dto = SimCardAssignmentHttpMapper.toDTO(result)

        assertThat(dto.deviceId).isEqualTo(deviceId)
        assertThat(dto.simCardId).isEqualTo(simCardId)
        assertThat(dto.assignedAt).isEqualTo(assignedAt)
        assertThat(dto.assignedBy).isEqualTo(assignedBy)
        assertThat(dto.unassignedAt).isNull()
        assertThat(dto.unassignedBy).isNull()
        assertThat(dto.removalReason).isNull()
        assertThat(dto.active).isTrue()
    }

    @Test
    @DisplayName("Should map unassign request to command")
    fun should_map_unassign_request_to_command() {

        val deviceId = UUID.randomUUID()
        val simCardId = UUID.randomUUID()
        val unassignedBy = UUID.randomUUID()

        val request = UnassignDeviceFromSimCardRequestDTO(
            deviceId,
            SimCardRemovalReason.UPGRADE
        )

        val command = SimCardAssignmentHttpMapper.toUnassignDeviceFromSimCardCommand(
            request,
            simCardId,
            unassignedBy
        )

        assertThat(command.deviceId).isEqualTo(deviceId)
        assertThat(command.simCardId).isEqualTo(simCardId)
        assertThat(command.unassignedBy).isEqualTo(unassignedBy)
        assertThat(command.removalReason).isEqualTo(SimCardRemovalReason.UPGRADE)
    }

}
