package com.github.diogocerqueiralima.asset.service.presentation.http.controllers

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.results.SimCardAssignmentResult
import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardRemovalReason
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.SimCardAssignmentUseCase
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.AssignDeviceToSimCardRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UnassignDeviceFromSimCardRequestDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class SimCardAssignmentControllerTest {

    @Mock
    private lateinit var simCardAssignmentUseCase: SimCardAssignmentUseCase

    @InjectMocks
    private lateinit var simCardAssignmentController: SimCardAssignmentController

    @Test
    @DisplayName("Should assign device to SIM card and return created response")
    fun should_assign_device_to_sim_card_and_return_created_response() {

        val userId = UUID.randomUUID()
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

        whenever(simCardAssignmentUseCase.assignDeviceToSimCard(any())).thenReturn(result)

        val request = AssignDeviceToSimCardRequestDTO(deviceId)

        val response = simCardAssignmentController.assignDeviceToSimCard(
            userId.toString(),
            simCardId,
            request
        )

        val commandCaptor = argumentCaptor<AssignDeviceToSimCardCommand>()
        verify(simCardAssignmentUseCase).assignDeviceToSimCard(commandCaptor.capture())

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("Device assigned to SIM card successfully.")
        assertThat(body.data).isNotNull()
        assertThat(body.data.deviceId).isEqualTo(deviceId)
        assertThat(body.data.simCardId).isEqualTo(simCardId)
        assertThat(body.data.active).isTrue()
        assertThat(commandCaptor.firstValue.assignedBy).isEqualTo(userId)
    }

    @Test
    @DisplayName("Should unassign device from SIM card and return ok response")
    fun should_unassign_device_from_sim_card_and_return_ok_response() {

        val userId = UUID.randomUUID()
        val deviceId = UUID.randomUUID()
        val simCardId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val assignedAt = Instant.parse("2026-03-20T12:00:00Z")

        val result = SimCardAssignmentResult(
            deviceId,
            simCardId,
            assignedAt,
            assignedBy,
            Instant.parse("2026-04-01T08:00:00Z"),
            UUID.randomUUID(),
            SimCardRemovalReason.UPGRADE,
            false
        )

        whenever(simCardAssignmentUseCase.unassignDeviceFromSimCard(any())).thenReturn(result)

        val request = UnassignDeviceFromSimCardRequestDTO(
            deviceId,
            SimCardRemovalReason.UPGRADE
        )

        val response = simCardAssignmentController.unassignDeviceFromSimCard(
            userId.toString(),
            simCardId,
            request
        )

        val commandCaptor = argumentCaptor<UnassignDeviceFromSimCardCommand>()
        verify(simCardAssignmentUseCase).unassignDeviceFromSimCard(commandCaptor.capture())

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull()
        val body = response.body!!
        assertThat(body.message).isEqualTo("Device unassigned from SIM card successfully.")
        assertThat(body.data).isNotNull()
        assertThat(body.data.deviceId).isEqualTo(deviceId)
        assertThat(body.data.simCardId).isEqualTo(simCardId)
        assertThat(body.data.unassignedAt).isNotNull()
        assertThat(body.data.unassignedBy).isNotNull()
        assertThat(body.data.removalReason).isEqualTo(SimCardRemovalReason.UPGRADE)
        assertThat(body.data.active).isFalse()
        assertThat(commandCaptor.firstValue.unassignedBy).isEqualTo(userId)
    }

}
