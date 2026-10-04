package com.github.diogocerqueiralima.asset.service.presentation.http.mappers;

import com.github.diogocerqueiralima.asset.service.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.DeleteSimCardByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetSimCardByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.asset.service.application.results.SimCardResult;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateSimCardRequestDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.SimCardDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UpdateSimCardRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SimCardHttpMapperTest {

    @Test
    @DisplayName("Should map create request to command")
    void should_map_create_request_to_command() {

        CreateSimCardRequestDTO request = new CreateSimCardRequestDTO("8901000000000000001", "351910000001", "268010000000001");
        CreateSimCardCommand command = SimCardHttpMapper.INSTANCE.toCreateCommand(request, UUID.randomUUID());

        assertThat(command.getIccid()).isEqualTo(request.getIccid());
        assertThat(command.getMsisdn()).isEqualTo(request.getMsisdn());
        assertThat(command.getImsi()).isEqualTo(request.getImsi());
    }

    @Test
    @DisplayName("Should map update request to command")
    void should_map_update_request_to_command() {

        UpdateSimCardRequestDTO request = new UpdateSimCardRequestDTO("8901000000000000001", "351910000002", "268010000000002");
        UpdateSimCardCommand command = SimCardHttpMapper.INSTANCE.toUpdateCommand(UUID.randomUUID(), request, UUID.randomUUID());

        assertThat(command.getIccid()).isEqualTo(request.getIccid());
        assertThat(command.getMsisdn()).isEqualTo(request.getMsisdn());
        assertThat(command.getImsi()).isEqualTo(request.getImsi());
    }

    @Test
    @DisplayName("Should map id to get command")
    void should_map_id_to_get_command() {

        UUID id = UUID.randomUUID();
        GetSimCardByIdCommand command = SimCardHttpMapper.INSTANCE.toGetByIdCommand(id, UUID.randomUUID());

        assertThat(command.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Should map id to delete command")
    void should_map_id_to_delete_command() {

        UUID id = UUID.randomUUID();
        DeleteSimCardByIdCommand command = SimCardHttpMapper.INSTANCE.toDeleteByIdCommand(id, UUID.randomUUID());

        assertThat(command.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Should map result to dto")
    void should_map_result_to_dto() {

        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        SimCardResult result = new SimCardResult(id, createdAt, updatedAt, "8901000000000000001", "351910000001", "268010000000001");
        SimCardDTO dto = SimCardHttpMapper.INSTANCE.toDTO(result);

        assertThat(dto.getId()).isEqualTo(result.getId());
        assertThat(dto.getCreatedAt()).isEqualTo(result.getCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(result.getUpdatedAt());
        assertThat(dto.getIccid()).isEqualTo(result.getIccid());
        assertThat(dto.getMsisdn()).isEqualTo(result.getMsisdn());
        assertThat(dto.getImsi()).isEqualTo(result.getImsi());
    }

}
