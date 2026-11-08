package com.github.diogocerqueiralima.asset.service.presentation.http.mappers

import com.github.diogocerqueiralima.asset.service.application.results.SimCardResult
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateSimCardRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UpdateSimCardRequestDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class SimCardHttpMapperTest {

    @Test
    @DisplayName("Should map create request to command")
    fun should_map_create_request_to_command() {

        val request = CreateSimCardRequestDTO("8901000000000000001", "351910000001", "268010000000001")
        val command = SimCardHttpMapper.toCreateCommand(request, UUID.randomUUID())

        assertThat(command.iccid).isEqualTo(request.iccid)
        assertThat(command.msisdn).isEqualTo(request.msisdn)
        assertThat(command.imsi).isEqualTo(request.imsi)
    }

    @Test
    @DisplayName("Should map update request to command")
    fun should_map_update_request_to_command() {

        val request = UpdateSimCardRequestDTO("8901000000000000001", "351910000002", "268010000000002")
        val command = SimCardHttpMapper.toUpdateCommand(UUID.randomUUID(), request, UUID.randomUUID())

        assertThat(command.iccid).isEqualTo(request.iccid)
        assertThat(command.msisdn).isEqualTo(request.msisdn)
        assertThat(command.imsi).isEqualTo(request.imsi)
    }

    @Test
    @DisplayName("Should map id to get command")
    fun should_map_id_to_get_command() {

        val id = UUID.randomUUID()
        val command = SimCardHttpMapper.toGetByIdCommand(id, UUID.randomUUID())

        assertThat(command.id).isEqualTo(id)
    }

    @Test
    @DisplayName("Should map id to delete command")
    fun should_map_id_to_delete_command() {

        val id = UUID.randomUUID()
        val command = SimCardHttpMapper.toDeleteByIdCommand(id, UUID.randomUUID())

        assertThat(command.id).isEqualTo(id)
    }

    @Test
    @DisplayName("Should map result to dto")
    fun should_map_result_to_dto() {

        val id = UUID.randomUUID()
        val createdAt = Instant.now()
        val updatedAt = Instant.now()
        val result = SimCardResult(id, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001")
        val dto = SimCardHttpMapper.toDTO(result)

        assertThat(dto.id).isEqualTo(result.id)
        assertThat(dto.createdAt).isEqualTo(result.createdAt)
        assertThat(dto.updatedAt).isEqualTo(result.updatedAt)
        assertThat(dto.iccid).isEqualTo(result.iccid)
        assertThat(dto.msisdn).isEqualTo(result.msisdn)
        assertThat(dto.imsi).isEqualTo(result.imsi)
    }

}
