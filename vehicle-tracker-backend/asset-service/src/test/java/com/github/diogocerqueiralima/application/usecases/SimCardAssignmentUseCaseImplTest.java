package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromSimCardCommand;
import com.github.diogocerqueiralima.application.exceptions.DeviceAlreadyAssignedException;
import com.github.diogocerqueiralima.application.exceptions.DeviceNotFoundException;
import com.github.diogocerqueiralima.application.exceptions.SimCardAlreadyAssignedException;
import com.github.diogocerqueiralima.application.exceptions.SimCardAssignmentNotFoundException;
import com.github.diogocerqueiralima.application.exceptions.SimCardNotFoundException;
import com.github.diogocerqueiralima.application.ports.outbound.DevicePersistence;
import com.github.diogocerqueiralima.application.ports.outbound.SimCardAssignmentPersistence;
import com.github.diogocerqueiralima.application.ports.outbound.SimCardPersistence;
import com.github.diogocerqueiralima.application.results.SimCardAssignmentResult;
import com.github.diogocerqueiralima.domain.assets.SimCard;
import com.github.diogocerqueiralima.domain.assignments.SimCardAssignment;
import com.github.diogocerqueiralima.domain.assignments.SimCardRemovalReason;
import com.github.diogocerqueiralima.domain.assets.Device;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimCardAssignmentUseCaseImplTest {

    @Mock
    private DevicePersistence devicePersistence;

    @Mock
    private SimCardPersistence simCardPersistence;

    @Mock
    private SimCardAssignmentPersistence simCardAssignmentPersistence;

    @InjectMocks
    private SimCardAssignmentUseCaseImpl simCardAssignmentUseCase;

    @Test
    @DisplayName("Should assign device to SIM card when both exist and are not active in other assignments")
    void should_assign_device_to_sim_card_when_both_exist_and_are_not_active_in_other_assignments() {

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

        SimCardAssignment savedAssignment = new SimCardAssignment(
                device,
                simCard,
                Instant.parse("2026-03-20T12:00:00Z"),
                null,
                assignedBy,
                null,
                null
        );

        when(devicePersistence.findById(deviceId)).thenReturn(Optional.of(device));
        when(simCardPersistence.findById(simCardId)).thenReturn(Optional.of(simCard));
        when(simCardAssignmentPersistence.existsActiveByDeviceId(deviceId)).thenReturn(false);
        when(simCardAssignmentPersistence.existsActiveBySimCardId(simCardId)).thenReturn(false);
        when(simCardAssignmentPersistence.save(any(SimCardAssignment.class))).thenReturn(savedAssignment);

        SimCardAssignmentResult result = simCardAssignmentUseCase.assignDeviceToSimCard(command);

        assertThat(result.deviceId()).isEqualTo(deviceId);
        assertThat(result.simCardId()).isEqualTo(simCardId);
        assertThat(result.assignedBy()).isEqualTo(assignedBy);
        assertThat(result.active()).isTrue();

        verify(simCardAssignmentPersistence).save(any(SimCardAssignment.class));
    }

    @Test
    @DisplayName("Should fail assigning when device does not exist")
    void should_fail_assigning_when_device_does_not_exist() {

        UUID deviceId = UUID.randomUUID();
        UUID simCardId = UUID.randomUUID();

        AssignDeviceToSimCardCommand command = new AssignDeviceToSimCardCommand(
                deviceId,
                simCardId,
                UUID.randomUUID()
        );

        when(devicePersistence.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> simCardAssignmentUseCase.assignDeviceToSimCard(command))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessage("Device not found for id: " + deviceId);

        verify(simCardAssignmentPersistence, never()).save(any(SimCardAssignment.class));
    }

    @Test
    @DisplayName("Should fail assigning when SIM card does not exist")
    void should_fail_assigning_when_sim_card_does_not_exist() {

        UUID deviceId = UUID.randomUUID();
        UUID simCardId = UUID.randomUUID();

        Device device = new Device(
                deviceId,
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                "SN-001",
                "TK-1000",
                "Teltonika",
                "123456789012345"
        );

        AssignDeviceToSimCardCommand command = new AssignDeviceToSimCardCommand(
                deviceId,
                simCardId,
                UUID.randomUUID()
        );

        when(devicePersistence.findById(deviceId)).thenReturn(Optional.of(device));
        when(simCardPersistence.findById(simCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> simCardAssignmentUseCase.assignDeviceToSimCard(command))
                .isInstanceOf(SimCardNotFoundException.class)
                .hasMessage("SIM card not found for id: " + simCardId);

        verify(simCardAssignmentPersistence, never()).save(any(SimCardAssignment.class));
    }

    @Test
    @DisplayName("Should fail assigning when device is already assigned")
    void should_fail_assigning_when_device_is_already_assigned() {

        UUID deviceId = UUID.randomUUID();
        UUID simCardId = UUID.randomUUID();

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
                "+351910000003",
                "268011234567893"
        );

        AssignDeviceToSimCardCommand command = new AssignDeviceToSimCardCommand(
                deviceId,
                simCardId,
                UUID.randomUUID()
        );

        when(devicePersistence.findById(deviceId)).thenReturn(Optional.of(device));
        when(simCardPersistence.findById(simCardId)).thenReturn(Optional.of(simCard));
        when(simCardAssignmentPersistence.existsActiveByDeviceId(deviceId)).thenReturn(true);

        assertThatThrownBy(() -> simCardAssignmentUseCase.assignDeviceToSimCard(command))
                .isInstanceOf(DeviceAlreadyAssignedException.class)
                .hasMessage("Device already assigned for id: " + deviceId);

        verify(simCardAssignmentPersistence, never()).save(any(SimCardAssignment.class));
    }

    @Test
    @DisplayName("Should fail assigning when SIM card is already assigned")
    void should_fail_assigning_when_sim_card_is_already_assigned() {

        UUID deviceId = UUID.randomUUID();
        UUID simCardId = UUID.randomUUID();

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
                "+351910000004",
                "268011234567894"
        );

        AssignDeviceToSimCardCommand command = new AssignDeviceToSimCardCommand(
                deviceId,
                simCardId,
                UUID.randomUUID()
        );

        when(devicePersistence.findById(deviceId)).thenReturn(Optional.of(device));
        when(simCardPersistence.findById(simCardId)).thenReturn(Optional.of(simCard));
        when(simCardAssignmentPersistence.existsActiveByDeviceId(deviceId)).thenReturn(false);
        when(simCardAssignmentPersistence.existsActiveBySimCardId(simCardId)).thenReturn(true);

        assertThatThrownBy(() -> simCardAssignmentUseCase.assignDeviceToSimCard(command))
                .isInstanceOf(SimCardAlreadyAssignedException.class)
                .hasMessage("SIM card already assigned for id: " + simCardId);

        verify(simCardAssignmentPersistence, never()).save(any(SimCardAssignment.class));
    }

    @Test
    @DisplayName("Should unassign device from SIM card when active assignment exists")
    void should_unassign_device_from_sim_card_when_active_assignment_exists() {

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
                "8991101200003204510",
                "+351910000005",
                "268011234567895"
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

        SimCardAssignment closedAssignment = new SimCardAssignment(
                device,
                simCard,
                activeAssignment.getAssignedAt(),
                Instant.parse("2026-04-01T08:00:00Z"),
                assignedBy,
                unassignedBy,
                SimCardRemovalReason.OTHER
        );

        UnassignDeviceFromSimCardCommand command = new UnassignDeviceFromSimCardCommand(
                deviceId,
                simCardId,
                unassignedBy,
                SimCardRemovalReason.OTHER
        );

        when(devicePersistence.findById(deviceId)).thenReturn(Optional.of(device));
        when(simCardPersistence.findById(simCardId)).thenReturn(Optional.of(simCard));
        when(simCardAssignmentPersistence.findActiveByDeviceIdAndSimCardId(deviceId, simCardId))
                .thenReturn(Optional.of(activeAssignment));
        when(simCardAssignmentPersistence.save(any(SimCardAssignment.class))).thenReturn(closedAssignment);

        SimCardAssignmentResult result = simCardAssignmentUseCase.unassignDeviceFromSimCard(command);

        assertThat(result.deviceId()).isEqualTo(deviceId);
        assertThat(result.simCardId()).isEqualTo(simCardId);
        assertThat(result.assignedBy()).isEqualTo(assignedBy);
        assertThat(result.active()).isFalse();

        verify(simCardAssignmentPersistence).save(any(SimCardAssignment.class));
    }

    @Test
    @DisplayName("Should fail unassigning when active assignment does not exist")
    void should_fail_unassigning_when_active_assignment_does_not_exist() {

        UUID deviceId = UUID.randomUUID();
        UUID simCardId = UUID.randomUUID();

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
                "+351910000006",
                "268011234567896"
        );

        UnassignDeviceFromSimCardCommand command = new UnassignDeviceFromSimCardCommand(
                deviceId,
                simCardId,
                UUID.randomUUID(),
                SimCardRemovalReason.UPGRADE
        );

        when(devicePersistence.findById(deviceId)).thenReturn(Optional.of(device));
        when(simCardPersistence.findById(simCardId)).thenReturn(Optional.of(simCard));
        when(simCardAssignmentPersistence.findActiveByDeviceIdAndSimCardId(deviceId, simCardId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> simCardAssignmentUseCase.unassignDeviceFromSimCard(command))
                .isInstanceOf(SimCardAssignmentNotFoundException.class)
                .hasMessage("Active SIM card assignment not found for device id: " + deviceId + " and SIM id: " + simCardId);

        verify(simCardAssignmentPersistence, never()).save(any(SimCardAssignment.class));
    }

}

