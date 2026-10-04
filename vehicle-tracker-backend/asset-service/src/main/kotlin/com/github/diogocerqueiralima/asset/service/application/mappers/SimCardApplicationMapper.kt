package com.github.diogocerqueiralima.asset.service.application.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.CreateSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.results.SimCardResult
import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard
import java.time.Instant
import java.util.UUID

/**
 * Mapper for SIM card conversions in the application layer.
 */
object SimCardApplicationMapper {

    /**
     * Builds a domain SIM card from a create command.
     *
     * @param command create command with the SIM card data.
     * @param now current timestamp for createdAt and updatedAt fields.
     * @return new domain SIM card with the provided data.
     */
    fun toDomain(command: CreateSimCardCommand, now: Instant): SimCard =
        SimCard(
            UUID.randomUUID(),
            command.userId,
            now,
            now,
            command.iccid,
            command.msisdn,
            command.imsi
        )

    /**
     * Builds a domain SIM card from an update command.
     *
     * @param command update command with the SIM card data.
     * @param existingSimCard existing SIM card to be updated.
     * @param updatedAt timestamp of the update operation.
     * @return updated domain SIM card with the provided data.
     */
    fun toDomain(command: UpdateSimCardCommand, existingSimCard: SimCard, updatedAt: Instant): SimCard =
        SimCard(
            command.id,
            existingSimCard.ownerId,
            existingSimCard.createdAt,
            updatedAt,
            command.iccid,
            command.msisdn,
            command.imsi
        )

    /**
     * Builds a SIM card application result from a domain SIM card.
     *
     * @param simCard domain SIM card.
     * @return SIM card application result.
     */
    fun toResult(simCard: SimCard): SimCardResult =
        SimCardResult(
            simCard.id,
            simCard.createdAt,
            simCard.updatedAt,
            simCard.iccid,
            simCard.msisdn,
            simCard.imsi
        )

}
