package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.domain.SimCard;

/**
 * Mapper for SIM card conversions in the application layer.
 */
public final class SimCardApplicationMapper {

    // Should not be instantiated
    private SimCardApplicationMapper() {}

    /**
     *
     * Builds a domain SIM card from a create command.
     *
     * @param command create command with the SIM card data.
     * @return new domain SIM card with the provided data.
     */
    public static SimCard toDomain(CreateSimCardCommand command) {
        return new SimCard(command.iccid(), command.msisdn(), command.imsi());
    }

    /**
     *
     * Builds a domain SIM card from an update command.
     *
     * @param command update command with the SIM card data.
     * @return updated domain SIM card with the provided data.
     */
    public static SimCard toDomain(UpdateSimCardCommand command) {
        return new SimCard(command.iccid(), command.msisdn(), command.imsi());
    }

    /**
     *
     * Builds a SIM card application result from a domain SIM card.
     *
     * @param simCard domain SIM card.
     * @return SIM card application result.
     */
    public static SimCardResult toResult(SimCard simCard) {
        return new SimCardResult(simCard.getIccid(), simCard.getMsisdn(), simCard.getImsi());
    }

}

