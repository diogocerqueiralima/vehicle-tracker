package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.domain.SimCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimCardApplicationMapperTest {

    @Test
    @DisplayName("Should map create command to domain")
    void should_map_create_command_to_domain() {
        CreateSimCardCommand command = new CreateSimCardCommand("8901000000000000001", "351910000001", "268010000000001");

        SimCard simCard = SimCardApplicationMapper.toDomain(command);

        assertThat(simCard.getIccid()).isEqualTo(command.iccid());
        assertThat(simCard.getMsisdn()).isEqualTo(command.msisdn());
        assertThat(simCard.getImsi()).isEqualTo(command.imsi());
    }

    @Test
    @DisplayName("Should map update command to domain")
    void should_map_update_command_to_domain() {
        UpdateSimCardCommand command = new UpdateSimCardCommand("8901000000000000001", "351910000002", "268010000000002");

        SimCard simCard = SimCardApplicationMapper.toDomain(command);

        assertThat(simCard.getIccid()).isEqualTo(command.iccid());
        assertThat(simCard.getMsisdn()).isEqualTo(command.msisdn());
        assertThat(simCard.getImsi()).isEqualTo(command.imsi());
    }

    @Test
    @DisplayName("Should map domain to result")
    void should_map_domain_to_result() {
        SimCard simCard = new SimCard("8901000000000000001", "351910000001", "268010000000001");

        SimCardResult result = SimCardApplicationMapper.toResult(simCard);

        assertThat(result.iccid()).isEqualTo(simCard.getIccid());
        assertThat(result.msisdn()).isEqualTo(simCard.getMsisdn());
        assertThat(result.imsi()).isEqualTo(simCard.getImsi());
    }

}

