package com.github.diogocerqueiralima.asset.service.application.mappers;

import com.github.diogocerqueiralima.asset.service.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.asset.service.application.results.SimCardResult;
import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimCardApplicationMapperTest {

    @Test
    @DisplayName("Should map create command to domain")
    void should_map_create_command_to_domain() {

        CreateSimCardCommand command = new CreateSimCardCommand("8901000000000000001", "351910000001", "268010000000001", UUID.randomUUID());
        SimCard simCard = SimCardApplicationMapper.INSTANCE.toDomain(command, Instant.now());

        assertThat(simCard.getIccid()).isEqualTo(command.getIccid());
        assertThat(simCard.getMsisdn()).isEqualTo(command.getMsisdn());
        assertThat(simCard.getImsi()).isEqualTo(command.getImsi());
    }

    @Test
    @DisplayName("Should map update command to domain")
    void should_map_update_command_to_domain() {

        UUID id = UUID.randomUUID();
        UpdateSimCardCommand command = new UpdateSimCardCommand(id, "8901000000000000001", "351910000002", "268010000000002", UUID.randomUUID());
        SimCard existingSimCard = new SimCard(
                id,
                Instant.parse("2026-03-10T10:00:00Z"),
                Instant.parse("2026-03-10T10:00:00Z"),
                "8901000000000000001",
                "351910000001",
                "268010000000001"
        );
        SimCard simCard = SimCardApplicationMapper.INSTANCE.toDomain(command, existingSimCard, Instant.now());

        assertThat(simCard.getId()).isEqualTo(id);
        assertThat(simCard.getIccid()).isEqualTo(command.getIccid());
        assertThat(simCard.getMsisdn()).isEqualTo(command.getMsisdn());
        assertThat(simCard.getImsi()).isEqualTo(command.getImsi());
    }

    @Test
    @DisplayName("Should map domain to result")
    void should_map_domain_to_result() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        SimCard simCard = new SimCard(id, null, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001");
        SimCardResult result = SimCardApplicationMapper.INSTANCE.toResult(simCard);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(result.getIccid()).isEqualTo(simCard.getIccid());
        assertThat(result.getMsisdn()).isEqualTo(simCard.getMsisdn());
        assertThat(result.getImsi()).isEqualTo(simCard.getImsi());
    }

}

