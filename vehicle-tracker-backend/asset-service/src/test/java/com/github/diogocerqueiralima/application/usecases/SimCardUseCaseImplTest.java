package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.DeleteSimCardByIccidCommand;
import com.github.diogocerqueiralima.application.commands.GetSimCardByIccidCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.application.exceptions.SimCardAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.SimCardNotFoundException;
import com.github.diogocerqueiralima.application.ports.outbound.SimCardPersistence;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.domain.SimCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
        CreateSimCardCommand command = new CreateSimCardCommand("8901000000000000001", "351910000001", "268010000000001");
        SimCard saved = new SimCard(command.iccid(), command.msisdn(), command.imsi());

        when(simCardPersistence.existsByIccid(command.iccid())).thenReturn(false);
        when(simCardPersistence.existsByMsisdn(command.msisdn())).thenReturn(false);
        when(simCardPersistence.existsByImsi(command.imsi())).thenReturn(false);
        when(simCardPersistence.save(any(SimCard.class))).thenReturn(saved);

        SimCardResult result = simCardUseCase.create(command);

        assertThat(result.iccid()).isEqualTo(command.iccid());
        assertThat(result.msisdn()).isEqualTo(command.msisdn());
        assertThat(result.imsi()).isEqualTo(command.imsi());
        verify(simCardPersistence).save(any(SimCard.class));
    }

    @Test
    @DisplayName("Should fail creating when ICCID already exists")
    void should_fail_creating_when_iccid_already_exists() {
        CreateSimCardCommand command = new CreateSimCardCommand("8901000000000000001", "351910000001", "268010000000001");

        when(simCardPersistence.existsByIccid(command.iccid())).thenReturn(true);

        assertThatThrownBy(() -> simCardUseCase.create(command))
                .isInstanceOf(SimCardAlreadyExistsException.class)
                .hasMessage("A SIM card with the provided ICCID already exists.");

        verify(simCardPersistence, never()).save(any(SimCard.class));
    }

    @Test
    @DisplayName("Should fail creating when MSISDN already exists")
    void should_fail_creating_when_msisdn_already_exists() {
        CreateSimCardCommand command = new CreateSimCardCommand("8901000000000000001", "351910000001", "268010000000001");

        when(simCardPersistence.existsByIccid(command.iccid())).thenReturn(false);
        when(simCardPersistence.existsByMsisdn(command.msisdn())).thenReturn(true);

        assertThatThrownBy(() -> simCardUseCase.create(command))
                .isInstanceOf(SimCardAlreadyExistsException.class)
                .hasMessage("A SIM card with the provided MSISDN already exists.");

        verify(simCardPersistence, never()).save(any(SimCard.class));
    }

    @Test
    @DisplayName("Should fail creating when IMSI already exists")
    void should_fail_creating_when_imsi_already_exists() {
        CreateSimCardCommand command = new CreateSimCardCommand("8901000000000000001", "351910000001", "268010000000001");

        when(simCardPersistence.existsByIccid(command.iccid())).thenReturn(false);
        when(simCardPersistence.existsByMsisdn(command.msisdn())).thenReturn(false);
        when(simCardPersistence.existsByImsi(command.imsi())).thenReturn(true);

        assertThatThrownBy(() -> simCardUseCase.create(command))
                .isInstanceOf(SimCardAlreadyExistsException.class)
                .hasMessage("A SIM card with the provided IMSI already exists.");

        verify(simCardPersistence, never()).save(any(SimCard.class));
    }

    @Test
    @DisplayName("Should update sim card when it exists and unique fields are valid")
    void should_update_sim_card_when_it_exists_and_unique_fields_are_valid() {
        String iccid = "8901000000000000001";
        SimCard existing = new SimCard(iccid, "351910000001", "268010000000001");
        UpdateSimCardCommand command = new UpdateSimCardCommand(iccid, "351910000002", "268010000000002");
        SimCard updated = new SimCard(iccid, command.msisdn(), command.imsi());

        when(simCardPersistence.findByIccid(iccid)).thenReturn(Optional.of(existing));
        when(simCardPersistence.existsByMsisdn(command.msisdn())).thenReturn(false);
        when(simCardPersistence.existsByImsi(command.imsi())).thenReturn(false);
        when(simCardPersistence.save(any(SimCard.class))).thenReturn(updated);

        SimCardResult result = simCardUseCase.update(command);

        assertThat(result.iccid()).isEqualTo(iccid);
        assertThat(result.msisdn()).isEqualTo("351910000002");
        assertThat(result.imsi()).isEqualTo("268010000000002");
    }

    @Test
    @DisplayName("Should fail updating when sim card does not exist")
    void should_fail_updating_when_sim_card_does_not_exist() {
        String iccid = "8901000000000000001";
        UpdateSimCardCommand command = new UpdateSimCardCommand(iccid, "351910000002", "268010000000002");

        when(simCardPersistence.findByIccid(iccid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> simCardUseCase.update(command))
                .isInstanceOf(SimCardNotFoundException.class)
                .hasMessage("Sim card not found for iccid: " + iccid);
    }

    @Test
    @DisplayName("Should fail updating when MSISDN belongs to another sim card")
    void should_fail_updating_when_msisdn_belongs_to_another_sim_card() {
        String iccid = "8901000000000000001";
        SimCard existing = new SimCard(iccid, "351910000001", "268010000000001");
        UpdateSimCardCommand command = new UpdateSimCardCommand(iccid, "351910000002", "268010000000001");

        when(simCardPersistence.findByIccid(iccid)).thenReturn(Optional.of(existing));
        when(simCardPersistence.existsByMsisdn(command.msisdn())).thenReturn(true);

        assertThatThrownBy(() -> simCardUseCase.update(command))
                .isInstanceOf(SimCardAlreadyExistsException.class)
                .hasMessage("A SIM card with the provided MSISDN already exists.");

        verify(simCardPersistence, never()).save(any(SimCard.class));
    }

    @Test
    @DisplayName("Should fail updating when IMSI belongs to another sim card")
    void should_fail_updating_when_imsi_belongs_to_another_sim_card() {
        String iccid = "8901000000000000001";
        SimCard existing = new SimCard(iccid, "351910000001", "268010000000001");
        UpdateSimCardCommand command = new UpdateSimCardCommand(iccid, "351910000001", "268010000000002");

        when(simCardPersistence.findByIccid(iccid)).thenReturn(Optional.of(existing));
        when(simCardPersistence.existsByImsi(command.imsi())).thenReturn(true);

        assertThatThrownBy(() -> simCardUseCase.update(command))
                .isInstanceOf(SimCardAlreadyExistsException.class)
                .hasMessage("A SIM card with the provided IMSI already exists.");

        verify(simCardPersistence, never()).save(any(SimCard.class));
    }

    @Test
    @DisplayName("Should get sim card by iccid when it exists")
    void should_get_sim_card_by_iccid_when_it_exists() {
        String iccid = "8901000000000000001";
        SimCard simCard = new SimCard(iccid, "351910000001", "268010000000001");

        when(simCardPersistence.findByIccid(iccid)).thenReturn(Optional.of(simCard));

        SimCardResult result = simCardUseCase.getByIccid(new GetSimCardByIccidCommand(iccid));

        assertThat(result.iccid()).isEqualTo(iccid);
        assertThat(result.msisdn()).isEqualTo(simCard.getMsisdn());
        assertThat(result.imsi()).isEqualTo(simCard.getImsi());
    }

    @Test
    @DisplayName("Should fail getting sim card by iccid when it does not exist")
    void should_fail_getting_sim_card_by_iccid_when_it_does_not_exist() {
        String iccid = "8901000000000000001";

        when(simCardPersistence.findByIccid(iccid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> simCardUseCase.getByIccid(new GetSimCardByIccidCommand(iccid)))
                .isInstanceOf(SimCardNotFoundException.class)
                .hasMessage("Sim card not found for iccid: " + iccid);
    }

    @Test
    @DisplayName("Should delete sim card by iccid when it exists")
    void should_delete_sim_card_by_iccid_when_it_exists() {
        String iccid = "8901000000000000001";

        when(simCardPersistence.existsByIccid(iccid)).thenReturn(true);

        simCardUseCase.deleteByIccid(new DeleteSimCardByIccidCommand(iccid));

        verify(simCardPersistence).deleteByIccid(iccid);
    }

    @Test
    @DisplayName("Should fail deleting sim card by iccid when it does not exist")
    void should_fail_deleting_sim_card_by_iccid_when_it_does_not_exist() {
        String iccid = "8901000000000000001";

        when(simCardPersistence.existsByIccid(iccid)).thenReturn(false);

        assertThatThrownBy(() -> simCardUseCase.deleteByIccid(new DeleteSimCardByIccidCommand(iccid)))
                .isInstanceOf(SimCardNotFoundException.class)
                .hasMessage("Sim card not found for iccid: " + iccid);

        verify(simCardPersistence, never()).deleteByIccid(iccid);
    }

}

