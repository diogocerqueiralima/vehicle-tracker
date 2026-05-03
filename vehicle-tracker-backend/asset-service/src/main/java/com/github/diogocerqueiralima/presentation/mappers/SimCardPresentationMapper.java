package com.github.diogocerqueiralima.presentation.mappers;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.DeleteSimCardByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetSimCardByIdCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.presentation.dto.CreateSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.SimCardDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateSimCardRequestDTO;

import java.util.UUID;

/**
 * Mapper for SIM card conversions in the presentation layer.
 */
public final class SimCardPresentationMapper {

    // Should not be instantiated
    private SimCardPresentationMapper() {}

    /**
     *
     * Builds a create command from an HTTP request payload.
     *
     * @param request request payload for SIM card creation.
     * @return command consumed by the application layer.
     */
    public static CreateSimCardCommand toCreateCommand(CreateSimCardRequestDTO request) {
        return new CreateSimCardCommand(request.iccid(), request.msisdn(), request.imsi());
    }

    /**
     *
     * Builds an update command from an HTTP path identifier and a request payload.
     *
     * @param id SIM card id from the request path.
     * @param request request payload for SIM card update.
     * @return command consumed by the application layer.
     */
    public static UpdateSimCardCommand toUpdateCommand(UUID id, UpdateSimCardRequestDTO request) {
        return new UpdateSimCardCommand(id, request.iccid(), request.msisdn(), request.imsi());
    }

    /**
     * Builds a get-by-id command from an HTTP path identifier.
     *
     * @param id SIM card id from the request path.
     * @return command consumed by the application layer.
     */
    public static GetSimCardByIdCommand toGetByIdCommand(UUID id) {
        return new GetSimCardByIdCommand(id);
    }

    /**
     * Builds a delete-by-id command from an HTTP path identifier.
     *
     * @param id SIM card id from the request path.
     * @return command consumed by the application layer.
     */
    public static DeleteSimCardByIdCommand toDeleteByIdCommand(UUID id) {
        return new DeleteSimCardByIdCommand(id);
    }

    /**
     *
     * Builds a transport DTO from an application result.
     *
     * @param result the application result to be converted into a transport DTO.
     * @return SIM card DTO payload.
     */
    public static SimCardDTO toDTO(SimCardResult result) {
        return new SimCardDTO(result.id(), result.createdAt(), result.updatedAt(), result.iccid(), result.msisdn(), result.imsi());
    }

}

