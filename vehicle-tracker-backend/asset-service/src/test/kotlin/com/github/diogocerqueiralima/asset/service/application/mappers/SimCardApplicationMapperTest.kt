package com.github.diogocerqueiralima.asset.service.application.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.CreateSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateSimCardCommand
import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class SimCardApplicationMapperTest {

    @Test
    @DisplayName("Should map create command to domain")
    fun should_map_create_command_to_domain() {

        val command = CreateSimCardCommand("8901000000000000001", "351910000001", "268010000000001", UUID.randomUUID())
        val simCard = SimCardApplicationMapper.toDomain(command, Instant.now())

        assertThat(simCard.iccid).isEqualTo(command.iccid)
        assertThat(simCard.msisdn).isEqualTo(command.msisdn)
        assertThat(simCard.imsi).isEqualTo(command.imsi)
    }

    @Test
    @DisplayName("Should map update command to domain")
    fun should_map_update_command_to_domain() {

        val id = UUID.randomUUID()
        val command = UpdateSimCardCommand(id, "8901000000000000001", "351910000002", "268010000000002", UUID.randomUUID())
        val existingSimCard = SimCard(
            id,
            Instant.parse("2026-03-10T10:00:00Z"),
            Instant.parse("2026-03-10T10:00:00Z"),
            "8901000000000000001",
            "351910000001",
            "268010000000001"
        )
        val simCard = SimCardApplicationMapper.toDomain(command, existingSimCard, Instant.now())

        assertThat(simCard.id).isEqualTo(id)
        assertThat(simCard.iccid).isEqualTo(command.iccid)
        assertThat(simCard.msisdn).isEqualTo(command.msisdn)
        assertThat(simCard.imsi).isEqualTo(command.imsi)
    }

    @Test
    @DisplayName("Should map domain to result")
    fun should_map_domain_to_result() {

        val id = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val simCard = SimCard(id, null, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001")
        val result = SimCardApplicationMapper.toResult(simCard)

        assertThat(result.id).isEqualTo(id)
        assertThat(result.createdAt).isEqualTo(createdAt)
        assertThat(result.updatedAt).isEqualTo(updatedAt)
        assertThat(result.iccid).isEqualTo(simCard.iccid)
        assertThat(result.msisdn).isEqualTo(simCard.msisdn)
        assertThat(result.imsi).isEqualTo(simCard.imsi)
    }

}
