package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.SimCard;
import com.github.diogocerqueiralima.infrastructure.entities.SimCardEntity;

/**
 * Mapper for SIM card conversions between domain and persistence layers.
 */
public class SimCardMapper {

    // Should not be instantiated
    private SimCardMapper() {}

    /**
     *
     * Builds a persistence entity from a domain SIM card.
     *
     * @param simCard domain SIM card with the data to be persisted.
     * @return persistence entity with the provided data ready for persistence.
     */
    public static SimCardEntity toEntity(SimCard simCard) {

        SimCardEntity entity = new SimCardEntity();

        entity.setIccid(simCard.getIccid());
        entity.setMsisdn(simCard.getMsisdn());
        entity.setImsi(simCard.getImsi());

        return entity;
    }

    /**
     *
     * Builds a domain SIM card from a persistence entity.
     *
     * @param entity persistence entity with the SIM card data.
     * @return domain SIM card with the provided data.
     */
    public static SimCard toDomain(SimCardEntity entity) {
        return new SimCard(
                entity.getIccid(),
                entity.getMsisdn(),
                entity.getImsi()
        );
    }

}
