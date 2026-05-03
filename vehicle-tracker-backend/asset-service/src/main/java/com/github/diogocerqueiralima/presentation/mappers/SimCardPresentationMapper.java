package com.github.diogocerqueiralima.presentation.mappers;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.DeleteSimCardByIccidCommand;
import com.github.diogocerqueiralima.application.commands.GetSimCardByIccidCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.presentation.dto.CreateSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.SimCardDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateSimCardRequestDTO;

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
     * @param iccid SIM card ICCID from the request path.
     * @param request request payload for SIM card update.
     * @return command consumed by the application layer.
     */
    public static UpdateSimCardCommand toUpdateCommand(String iccid, UpdateSimCardRequestDTO request) {
        return new UpdateSimCardCommand(iccid, request.msisdn(), request.imsi());
    }

    /**
     * Builds a get-by-iccid command from an HTTP path identifier.
     *
     * @param iccid SIM card ICCID from the request path.
     * @return command consumed by the application layer.
     */
    public static GetSimCardByIccidCommand toGetByIccidCommand(String iccid) {
        return new GetSimCardByIccidCommand(iccid);
    }

    /**
     * Builds a delete-by-iccid command from an HTTP path identifier.
     *
     * @param iccid SIM card ICCID from the request path.
     * @return command consumed by the application layer.
     */
    public static DeleteSimCardByIccidCommand toDeleteByIccidCommand(String iccid) {
        return new DeleteSimCardByIccidCommand(iccid);
    }

    /**
     *
     * Builds a transport DTO from an application result.
     *
     * @param result the application result to be converted into a transport DTO.
     * @return SIM card DTO payload.
     */
    public static SimCardDTO toDTO(SimCardResult result) {
        return new SimCardDTO(result.iccid(), result.msisdn(), result.imsi());
    }

}

