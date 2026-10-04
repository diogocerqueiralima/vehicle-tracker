package com.github.diogocerqueiralima.asset.service.infrastructure.mappers

import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard
import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.SimCardEntity

object SimCardMapper {

    fun toEntity(simCard: SimCard): SimCardEntity =
        SimCardEntity().apply {
            id = simCard.id
            ownerId = simCard.ownerId
            createdAt = simCard.createdAt
            updatedAt = simCard.updatedAt
            iccid = simCard.iccid
            msisdn = simCard.msisdn
            imsi = simCard.imsi
        }

    fun toDomain(entity: SimCardEntity): SimCard =
        SimCard(
            entity.id,
            entity.ownerId,
            entity.createdAt,
            entity.updatedAt,
            entity.iccid,
            entity.msisdn,
            entity.imsi
        )

}
