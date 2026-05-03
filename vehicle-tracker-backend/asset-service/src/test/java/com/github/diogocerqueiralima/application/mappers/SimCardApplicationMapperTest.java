package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.domain.assets.SimCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimCardApplicationMapperTest {

    @Test
    @DisplayName("Should map create command to domain")
    void should_map_create_command_to_domain() {

        CreateSimCardCommand command = new CreateSimCardCommand("8901000000000000001", "351910000001", "268010000000001");
        SimCard simCard = SimCardApplicationMapper.toDomain(command, Instant.now());

        assertThat(simCard.getIccid()).isEqualTo(command.iccid());
        assertThat(simCard.getMsisdn()).isEqualTo(command.msisdn());
        assertThat(simCard.getImsi()).isEqualTo(command.imsi());
    }

    @Test
    @DisplayName("Should map update command to domain")
    void should_map_update_command_to_domain() {

        UUID id = UUID.randomUUID();
        UpdateSimCardCommand command = new UpdateSimCardCommand(id, "8901000000000000001", "351910000002", "268010000000002");
        SimCard existingSimCard = new SimCard(
                id,
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                "8901000000000000001",
                "351910000001",
                "268010000000001"
        );
        SimCard simCard = SimCardApplicationMapper.toDomain(command, existingSimCard, Instant.now());

        assertThat(simCard.getId()).isEqualTo(id);
        assertThat(simCard.getIccid()).isEqualTo(command.iccid());
        assertThat(simCard.getMsisdn()).isEqualTo(command.msisdn());
        assertThat(simCard.getImsi()).isEqualTo(command.imsi());
    }

    @Test
    @DisplayName("Should map domain to result")
    void should_map_domain_to_result() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        SimCard simCard = new SimCard(id, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001");
        SimCardResult result = SimCardApplicationMapper.toResult(simCard);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.updatedAt()).isEqualTo(updatedAt);
        assertThat(result.iccid()).isEqualTo(simCard.getIccid());
        assertThat(result.msisdn()).isEqualTo(simCard.getMsisdn());
        assertThat(result.imsi()).isEqualTo(simCard.getImsi());
    }

}

