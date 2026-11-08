package com.github.diogocerqueiralima.asset.service.application.usecases

import com.github.diogocerqueiralima.asset.service.application.commands.AssignDeviceToSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UnassignDeviceFromSimCardCommand
import com.github.diogocerqueiralima.asset.service.domain.assets.Device
import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard
import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardAssignment
import com.github.diogocerqueiralima.asset.service.domain.assignments.SimCardRemovalReason
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.DevicePersistence
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.SimCardAssignmentPersistence
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.SimCardPersistence
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
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class SimCardAssignmentUseCaseImplTest {

    @Mock
    private lateinit var devicePersistence: DevicePersistence

    @Mock
    private lateinit var simCardPersistence: SimCardPersistence

    @Mock
    private lateinit var simCardAssignmentPersistence: SimCardAssignmentPersistence

    @InjectMocks
    private lateinit var simCardAssignmentUseCase: SimCardAssignmentUseCaseImpl

    @Test
    @DisplayName("Should assign device to SIM card when both exist and are not active in other assignments")
    fun should_assign_device_to_sim_card_when_both_exist_and_are_not_active_in_other_assignments() {

        val deviceId = UUID.randomUUID()
        val simCardId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()

        val device = Device(
            deviceId,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val simCard = SimCard(
            simCardId,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "8991101200003204510",
            "+351910000000",
            "268011234567890"
        )

        val command = AssignDeviceToSimCardCommand(
            deviceId,
            simCardId,
            assignedBy
        )

        val savedAssignment = SimCardAssignment(
            device,
            simCard,
            Instant.parse("2026-03-20T12:00:00Z"),
            null,
            assignedBy,
            null,
            null
        )

        whenever(devicePersistence.findByIdAndOwnerId(deviceId, assignedBy)).thenReturn(device)
        whenever(simCardPersistence.findByIdAndOwnerId(simCardId, assignedBy)).thenReturn(simCard)
        whenever(simCardAssignmentPersistence.save(any())).thenReturn(savedAssignment)

        val result = simCardAssignmentUseCase.assignDeviceToSimCard(command)

        assertThat(result.deviceId).isEqualTo(deviceId)
        assertThat(result.simCardId).isEqualTo(simCardId)
        assertThat(result.assignedBy).isEqualTo(assignedBy)
        assertThat(result.active).isTrue()

        verify(simCardAssignmentPersistence).save(any())
    }

    @Test
    @DisplayName("Should fail assigning when device does not exist")
    fun should_fail_assigning_when_device_does_not_exist() {

        val deviceId = UUID.randomUUID()
        val simCardId = UUID.randomUUID()

        val command = AssignDeviceToSimCardCommand(
            deviceId,
            simCardId,
            UUID.randomUUID()
        )

        whenever(devicePersistence.findByIdAndOwnerId(deviceId, command.assignedBy)).thenReturn(null)

        assertThatThrownBy { simCardAssignmentUseCase.assignDeviceToSimCard(command) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Device not found for id: $deviceId")

        verify(simCardAssignmentPersistence, never()).save(any())
    }

    @Test
    @DisplayName("Should fail assigning when SIM card does not exist")
    fun should_fail_assigning_when_sim_card_does_not_exist() {

        val deviceId = UUID.randomUUID()
        val simCardId = UUID.randomUUID()

        val device = Device(
            deviceId,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val command = AssignDeviceToSimCardCommand(
            deviceId,
            simCardId,
            UUID.randomUUID()
        )

        whenever(devicePersistence.findByIdAndOwnerId(deviceId, command.assignedBy)).thenReturn(device)
        whenever(simCardPersistence.findByIdAndOwnerId(simCardId, command.assignedBy)).thenReturn(null)

        assertThatThrownBy { simCardAssignmentUseCase.assignDeviceToSimCard(command) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("SIM card not found for id: $simCardId")

        verify(simCardAssignmentPersistence, never()).save(any())
    }

    @Test
    @DisplayName("Should unassign device from SIM card when active assignment exists")
    fun should_unassign_device_from_sim_card_when_active_assignment_exists() {

        val deviceId = UUID.randomUUID()
        val simCardId = UUID.randomUUID()
        val assignedBy = UUID.randomUUID()
        val unassignedBy = UUID.randomUUID()

        val device = Device(
            deviceId,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val simCard = SimCard(
            simCardId,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "8991101200003204510",
            "+351910000005",
            "268011234567895"
        )

        val activeAssignment = SimCardAssignment(
            device,
            simCard,
            Instant.parse("2026-03-20T12:00:00Z"),
            null,
            assignedBy,
            null,
            null
        )

        val closedAssignment = SimCardAssignment(
            device,
            simCard,
            activeAssignment.assignedAt,
            Instant.parse("2026-04-01T08:00:00Z"),
            assignedBy,
            unassignedBy,
            SimCardRemovalReason.OTHER
        )

        val command = UnassignDeviceFromSimCardCommand(
            deviceId,
            simCardId,
            unassignedBy,
            SimCardRemovalReason.OTHER
        )

        whenever(devicePersistence.findByIdAndOwnerId(deviceId, command.unassignedBy)).thenReturn(device)
        whenever(simCardPersistence.findByIdAndOwnerId(simCardId, command.unassignedBy)).thenReturn(simCard)
        whenever(simCardAssignmentPersistence.findActiveByDeviceIdAndSimCardId(deviceId, simCardId))
            .thenReturn(activeAssignment)
        whenever(simCardAssignmentPersistence.save(any())).thenReturn(closedAssignment)

        val result = simCardAssignmentUseCase.unassignDeviceFromSimCard(command)

        assertThat(result.deviceId).isEqualTo(deviceId)
        assertThat(result.simCardId).isEqualTo(simCardId)
        assertThat(result.assignedBy).isEqualTo(assignedBy)
        assertThat(result.active).isFalse()

        verify(simCardAssignmentPersistence).save(any())
    }

    @Test
    @DisplayName("Should fail unassigning when active assignment does not exist")
    fun should_fail_unassigning_when_active_assignment_does_not_exist() {

        val deviceId = UUID.randomUUID()
        val simCardId = UUID.randomUUID()

        val device = Device(
            deviceId,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "SN-001",
            "TK-1000",
            "Teltonika",
            "123456789012345"
        )

        val simCard = SimCard(
            simCardId,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "8991101200003204510",
            "+351910000006",
            "268011234567896"
        )

        val command = UnassignDeviceFromSimCardCommand(
            deviceId,
            simCardId,
            UUID.randomUUID(),
            SimCardRemovalReason.UPGRADE
        )

        whenever(devicePersistence.findByIdAndOwnerId(deviceId, command.unassignedBy)).thenReturn(device)
        whenever(simCardPersistence.findByIdAndOwnerId(simCardId, command.unassignedBy)).thenReturn(simCard)
        whenever(simCardAssignmentPersistence.findActiveByDeviceIdAndSimCardId(deviceId, simCardId))
            .thenReturn(null)

        assertThatThrownBy { simCardAssignmentUseCase.unassignDeviceFromSimCard(command) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Active SIM card assignment not found for device id: $deviceId and SIM id: $simCardId")

        verify(simCardAssignmentPersistence, never()).save(any())
    }

}
