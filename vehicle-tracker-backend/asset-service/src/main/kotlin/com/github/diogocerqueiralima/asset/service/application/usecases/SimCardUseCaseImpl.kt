package com.github.diogocerqueiralima.asset.service.application.usecases

import com.github.diogocerqueiralima.error.common.exceptions.ConflictException
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.asset.service.application.commands.CreateSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.commands.DeleteSimCardByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetSimCardByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateSimCardCommand
import com.github.diogocerqueiralima.asset.service.application.mappers.SimCardApplicationMapper
import com.github.diogocerqueiralima.asset.service.application.results.SimCardResult
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.SimCardUseCase
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.SimCardPersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Application-layer implementation that orchestrates SIM card use cases.
 */
@Service
class SimCardUseCaseImpl(private val simCardPersistence: SimCardPersistence) : SimCardUseCase {

    override fun create(command: CreateSimCardCommand): SimCardResult {

        // 1. Fails when the ICCID, MSISDN or IMSI is already in use.
        if (simCardPersistence.existsByIccidOrMsisdnOrImsi(command.iccid, command.msisdn, command.imsi)) {
            throw ConflictException("A SIM card with the provided ICCID, MSISDN or IMSI already exists.")
        }

        // 2. Creates and saves the new SIM card.
        val simCardToSave = SimCardApplicationMapper.toDomain(command, Instant.now())
        val savedSimCard = simCardPersistence.save(simCardToSave)

        // 3. Builds the result.
        return SimCardApplicationMapper.toResult(savedSimCard)
    }

    @Transactional
    override fun update(command: UpdateSimCardCommand): SimCardResult {

        val id = command.id

        // 1. Gets the SIM card with the provided id constrained to owner.
        val existingSimCard = simCardPersistence.findByIdAndOwnerId(id, command.userId)
            ?: throw NotFoundException("SIM card not found for id: $id")

        // 2. Fails fast when another SIM card already uses the ICCID, MSISDN or IMSI.
        if (simCardPersistence.isIccidOrMsisdnOrImsiTakenByAnotherSimCard(command.iccid, command.msisdn, command.imsi, id)) {
            throw ConflictException("A SIM card with the provided ICCID, MSISDN or IMSI already exists.")
        }

        // 3. Updates and saves the SIM card.
        val simCardToSave = SimCardApplicationMapper.toDomain(command, existingSimCard, Instant.now())
        val updatedSimCard = simCardPersistence.save(simCardToSave)

        // 4. Builds the result.
        return SimCardApplicationMapper.toResult(updatedSimCard)
    }

    /**
     * Retrieves a SIM card by id.
     *
     * @param command get-by-id payload.
     * @return the matching SIM card as a result object.
     */
    override fun getById(command: GetSimCardByIdCommand): SimCardResult {

        // 1. Loads by id constrained to owner and fail fast when absent.
        val simCard = simCardPersistence.findByIdAndOwnerId(command.id, command.userId)
            ?: throw NotFoundException("SIM card not found for id: ${command.id}")

        // 2. Maps the domain object to the response contract.
        return SimCardApplicationMapper.toResult(simCard)
    }

    /**
     * Deletes a SIM card by id.
     *
     * @param command delete-by-id payload.
     */
    @Transactional
    override fun deleteById(command: DeleteSimCardByIdCommand) {

        val id = command.id
        val userId = command.userId

        // 1. Fails fast when the SIM card does not exist for the owner.
        if (simCardPersistence.findByIdAndOwnerId(id, userId) == null) {
            throw NotFoundException("SIM card not found for id: $id")
        }

        // 2. Deletes the SIM card from persistence constrained to owner.
        simCardPersistence.deleteByIdAndOwnerId(id, userId)
    }

}
