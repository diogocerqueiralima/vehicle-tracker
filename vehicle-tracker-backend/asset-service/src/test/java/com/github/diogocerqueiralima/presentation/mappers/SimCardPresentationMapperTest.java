package com.github.diogocerqueiralima.presentation.mappers;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.DeleteSimCardByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetSimCardByIdCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.presentation.dto.CreateSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.SimCardDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateSimCardRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimCardPresentationMapperTest {

    @Test
    @DisplayName("Should map create request to command")
    void should_map_create_request_to_command() {

        CreateSimCardRequestDTO request = new CreateSimCardRequestDTO("8901000000000000001", "351910000001", "268010000000001");
        CreateSimCardCommand command = SimCardPresentationMapper.toCreateCommand(request);

        assertThat(command.iccid()).isEqualTo(request.iccid());
        assertThat(command.msisdn()).isEqualTo(request.msisdn());
        assertThat(command.imsi()).isEqualTo(request.imsi());
    }

    @Test
    @DisplayName("Should map update request to command")
    void should_map_update_request_to_command() {

        UpdateSimCardRequestDTO request = new UpdateSimCardRequestDTO("8901000000000000001", "351910000002", "268010000000002");
        UpdateSimCardCommand command = SimCardPresentationMapper.toUpdateCommand(UUID.randomUUID(), request);

        assertThat(command.iccid()).isEqualTo(request.iccid());
        assertThat(command.msisdn()).isEqualTo(request.msisdn());
        assertThat(command.imsi()).isEqualTo(request.imsi());
    }

    @Test
    @DisplayName("Should map id to get command")
    void should_map_id_to_get_command() {

        UUID id = UUID.randomUUID();
        GetSimCardByIdCommand command = SimCardPresentationMapper.toGetByIdCommand(id);

        assertThat(command.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("Should map id to delete command")
    void should_map_id_to_delete_command() {

        UUID id = UUID.randomUUID();
        DeleteSimCardByIdCommand command = SimCardPresentationMapper.toDeleteByIdCommand(id);

        assertThat(command.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("Should map result to dto")
    void should_map_result_to_dto() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        SimCardResult result = new SimCardResult(id, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001");
        SimCardDTO dto = SimCardPresentationMapper.toDTO(result);

        assertThat(dto.id()).isEqualTo(result.id());
        assertThat(dto.createdAt()).isEqualTo(result.createdAt());
        assertThat(dto.updatedAt()).isEqualTo(result.updatedAt());
        assertThat(dto.iccid()).isEqualTo(result.iccid());
        assertThat(dto.msisdn()).isEqualTo(result.msisdn());
        assertThat(dto.imsi()).isEqualTo(result.imsi());
    }

}

