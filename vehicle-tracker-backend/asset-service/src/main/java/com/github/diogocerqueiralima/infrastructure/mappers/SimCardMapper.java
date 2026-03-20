package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.SimCard;
import com.github.diogocerqueiralima.infrastructure.entities.SimCardEntity;

public class SimCardMapper {

    // Should not be instantiated
    private SimCardMapper() {}

    public static SimCardEntity toEntity(SimCard simCard) {

        SimCardEntity entity = new SimCardEntity();

        entity.setIccid(simCard.getIccid());
        entity.setMsisdn(simCard.getMsisdn());
        entity.setImsi(simCard.getImsi());

        return entity;
    }

    public static SimCard toDomain(SimCardEntity entity) {
        return new SimCard(
                entity.getIccid(),
                entity.getMsisdn(),
                entity.getImsi()
        );
    }

}
