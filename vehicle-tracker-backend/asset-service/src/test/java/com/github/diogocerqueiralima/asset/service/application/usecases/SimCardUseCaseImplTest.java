package com.github.diogocerqueiralima.asset.service.application.usecases;

import com.github.diogocerqueiralima.asset.service.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.DeleteSimCardByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetSimCardByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.asset.service.application.exceptions.SimCardNotFoundException;
import com.github.diogocerqueiralima.asset.service.domain.exceptions.SimCardAlreadyExistsException;
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.SimCardPersistence;
import com.github.diogocerqueiralima.asset.service.application.results.SimCardResult;
import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard;
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
class SimCardUseCaseImplTest {

    @Mock
    private SimCardPersistence simCardPersistence;

    @InjectMocks
    private SimCardUseCaseImpl simCardUseCase;

    @Test
    @DisplayName("Should create sim card when unique fields are available")
    void should_create_sim_card_when_unique_fields_are_available() {

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        CreateSimCardCommand command = new CreateSimCardCommand("8901000000000000001", "351910000001", "268010000000001", ownerId);
        SimCard saved = new SimCard(id, ownerId, createdAt, updatedAt, command.iccid(), command.msisdn(), command.imsi());

        when(simCardPersistence.save(any(SimCard.class))).thenReturn(saved);

        SimCardResult result = simCardUseCase.create(command);

        assertThat(result.iccid()).isEqualTo(command.iccid());
        assertThat(result.msisdn()).isEqualTo(command.msisdn());
        assertThat(result.imsi()).isEqualTo(command.imsi());
        verify(simCardPersistence).save(any(SimCard.class));
    }

    @Test
    @DisplayName("Should fail creating when ICCID, MSISDN or IMSI already exists")
    void should_fail_creating_when_unique_field_already_exists() {

        CreateSimCardCommand command = new CreateSimCardCommand("8901000000000000001", "351910000001", "268010000000001", null);
        when(simCardPersistence.save(any(SimCard.class))).thenThrow(new SimCardAlreadyExistsException());

        assertThatThrownBy(() -> simCardUseCase.create(command))
                .isInstanceOf(SimCardAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should update sim card when it exists and unique fields are valid")
    void should_update_sim_card_when_it_exists_and_unique_fields_are_valid() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        UUID ownerId = UUID.randomUUID();
        SimCard existing = new SimCard(id, ownerId, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001");
        UpdateSimCardCommand command = new UpdateSimCardCommand(id, "8901000000000000001", "351910000002", "268010000000002", ownerId);
        SimCard updated = new SimCard(id, ownerId, createdAt, updatedAt, command.iccid(), command.msisdn(), command.imsi());

        when(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(existing));
        when(simCardPersistence.save(any(SimCard.class))).thenReturn(updated);

        SimCardResult result = simCardUseCase.update(command);

        assertThat(result.iccid()).isEqualTo("8901000000000000001");
        assertThat(result.msisdn()).isEqualTo("351910000002");
        assertThat(result.imsi()).isEqualTo("268010000000002");
    }

    @Test
    @DisplayName("Should fail updating when sim card does not exist")
    void should_fail_updating_when_sim_card_does_not_exist() {

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UpdateSimCardCommand command = new UpdateSimCardCommand(id, "8901000000000000001", "351910000002", "268010000000002", ownerId);

        when(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> simCardUseCase.update(command))
                .isInstanceOf(SimCardNotFoundException.class)
                .hasMessage("SIM card not found for id: " + id);
    }

    @Test
    @DisplayName("Should fail updating when MSISDN or IMSI belongs to another sim card")
    void should_fail_updating_when_unique_field_belongs_to_another_sim_card() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        UUID ownerId = UUID.randomUUID();
        SimCard existing = new SimCard(id, ownerId, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001");
        UpdateSimCardCommand command = new UpdateSimCardCommand(id, "8901000000000000001", "351910000002", "268010000000002", ownerId);

        when(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(existing));
        when(simCardPersistence.save(any(SimCard.class))).thenThrow(new SimCardAlreadyExistsException());

        assertThatThrownBy(() -> simCardUseCase.update(command))
                .isInstanceOf(SimCardAlreadyExistsException.class);

        verify(simCardPersistence).save(any(SimCard.class));
    }

    @Test
    @DisplayName("Should get sim card by id when it exists")
    void should_get_sim_card_by_id_when_it_exists() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        UUID ownerId = UUID.randomUUID();
        SimCard simCard = new SimCard(id, ownerId, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001");

        when(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(simCard));

        SimCardResult result = simCardUseCase.getById(new GetSimCardByIdCommand(id, ownerId));

        assertThat(result.iccid()).isEqualTo(simCard.getIccid());
        assertThat(result.msisdn()).isEqualTo(simCard.getMsisdn());
        assertThat(result.imsi()).isEqualTo(simCard.getImsi());
    }

    @Test
    @DisplayName("Should fail getting sim card by id when it does not exist")
    void should_fail_getting_sim_card_by_id_when_it_does_not_exist() {

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> simCardUseCase.getById(new GetSimCardByIdCommand(id, ownerId)))
                .isInstanceOf(SimCardNotFoundException.class)
                .hasMessage("SIM card not found for id: " + id);
    }

    @Test
    @DisplayName("Should delete sim card by id when it exists")
    void should_delete_sim_card_by_id_when_it_exists() {

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        SimCard existing = new SimCard(id, ownerId, Instant.now(), Instant.now(), "8901000000000000001", "351910000001", "268010000000001");

        when(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(existing));

        simCardUseCase.deleteById(new DeleteSimCardByIdCommand(id, ownerId));

        verify(simCardPersistence).deleteByIdAndOwnerId(id, ownerId);
    }

    @Test
    @DisplayName("Should fail deleting sim card by id when it does not exist")
    void should_fail_deleting_sim_card_by_id_when_it_does_not_exist() {

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(simCardPersistence.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> simCardUseCase.deleteById(new DeleteSimCardByIdCommand(id, ownerId)))
                .isInstanceOf(SimCardNotFoundException.class)
                .hasMessage("SIM card not found for id: " + id);

        verify(simCardPersistence, never()).deleteByIdAndOwnerId(id, ownerId);
    }

}
