package com.github.diogocerqueiralima.presentation.mappers;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.DeleteSimCardByIccidCommand;
import com.github.diogocerqueiralima.application.commands.GetSimCardByIccidCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.presentation.dto.CreateSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.SimCardDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateSimCardRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        UpdateSimCardRequestDTO request = new UpdateSimCardRequestDTO("351910000002", "268010000000002");

        UpdateSimCardCommand command = SimCardPresentationMapper.toUpdateCommand("8901000000000000001", request);

        assertThat(command.iccid()).isEqualTo("8901000000000000001");
        assertThat(command.msisdn()).isEqualTo(request.msisdn());
        assertThat(command.imsi()).isEqualTo(request.imsi());
    }

    @Test
    @DisplayName("Should map iccid to get command")
    void should_map_iccid_to_get_command() {
        GetSimCardByIccidCommand command = SimCardPresentationMapper.toGetByIccidCommand("8901000000000000001");

        assertThat(command.iccid()).isEqualTo("8901000000000000001");
    }

    @Test
    @DisplayName("Should map iccid to delete command")
    void should_map_iccid_to_delete_command() {
        DeleteSimCardByIccidCommand command = SimCardPresentationMapper.toDeleteByIccidCommand("8901000000000000001");

        assertThat(command.iccid()).isEqualTo("8901000000000000001");
    }

    @Test
    @DisplayName("Should map result to dto")
    void should_map_result_to_dto() {
        SimCardResult result = new SimCardResult("8901000000000000001", "351910000001", "268010000000001");

        SimCardDTO dto = SimCardPresentationMapper.toDTO(result);

        assertThat(dto.iccid()).isEqualTo(result.iccid());
        assertThat(dto.msisdn()).isEqualTo(result.msisdn());
        assertThat(dto.imsi()).isEqualTo(result.imsi());
    }

}

