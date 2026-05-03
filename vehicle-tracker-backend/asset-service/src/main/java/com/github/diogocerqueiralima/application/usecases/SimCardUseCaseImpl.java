package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.DeleteSimCardByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetSimCardByIdCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.application.exceptions.SimCardAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.SimCardNotFoundException;
import com.github.diogocerqueiralima.application.mappers.SimCardApplicationMapper;
import com.github.diogocerqueiralima.application.ports.inbound.SimCardUseCase;
import com.github.diogocerqueiralima.application.ports.outbound.SimCardPersistence;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.domain.assets.SimCard;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Application-layer implementation that orchestrates SIM card use cases.
 */
@Service
public class SimCardUseCaseImpl implements SimCardUseCase {

    private final SimCardPersistence simCardPersistence;

    public SimCardUseCaseImpl(SimCardPersistence simCardPersistence) {
        this.simCardPersistence = simCardPersistence;
    }

    @Override
    public SimCardResult create(CreateSimCardCommand command) {

        // 1. Checks if exists a SIM card with the provided ICCID.
        if (simCardPersistence.existsByIccid(command.iccid())) {
            throw new SimCardAlreadyExistsException("A SIM card with the provided ICCID already exists.");
        }

        // 2. Checks if exists a SIM card with the provided MSISDN.
        if (simCardPersistence.existsByMsisdn(command.msisdn())) {
            throw new SimCardAlreadyExistsException("A SIM card with the provided MSISDN already exists.");
        }

        // 3. Checks if exists a SIM card with the provided IMSI.
        if (simCardPersistence.existsByImsi(command.imsi())) {
            throw new SimCardAlreadyExistsException("A SIM card with the provided IMSI already exists.");
        }

        // 4. Creates and saves the new SIM card.
        SimCard simCardToSave = SimCardApplicationMapper.toDomain(command, Instant.now());
        SimCard savedSimCard = simCardPersistence.save(simCardToSave);

        // 5. Builds the result.
        return SimCardApplicationMapper.toResult(savedSimCard);
    }

    @Override
    public SimCardResult update(UpdateSimCardCommand command) {

        UUID id = command.id();
        UUID userId = command.userId();

        // 1. Gets the SIM card with the provided id constrained to owner.
        SimCard existingSimCard = simCardPersistence.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new SimCardNotFoundException(id));

        // 2. Checks if exists another SIM card with the given ICCID.
        if (!existingSimCard.getIccid().equals(command.iccid())
                && simCardPersistence.existsByIccid(command.iccid())) {
            throw new SimCardAlreadyExistsException("A SIM card with the provided ICCID already exists.");
        }

        // 3. Checks if exists another SIM card with the given MSISDN.
        if (!existingSimCard.getMsisdn().equals(command.msisdn())
                && simCardPersistence.existsByMsisdn(command.msisdn())) {
            throw new SimCardAlreadyExistsException("A SIM card with the provided MSISDN already exists.");
        }

        // 4. Checks if exists another SIM card with the given IMSI.
        if (!existingSimCard.getImsi().equals(command.imsi())
                && simCardPersistence.existsByImsi(command.imsi())) {
            throw new SimCardAlreadyExistsException("A SIM card with the provided IMSI already exists.");
        }

        // 5. Updates and saves the SIM card.
        SimCard simCardToSave = SimCardApplicationMapper.toDomain(command, existingSimCard, Instant.now());
        SimCard updatedSimCard = simCardPersistence.save(simCardToSave);

        // 6. Builds the result.
        return SimCardApplicationMapper.toResult(updatedSimCard);
    }

    /**
     * Retrieves a SIM card by id.
     *
     * @param command get-by-id payload.
     * @return the matching SIM card as a result object.
     */
    @Override
    public SimCardResult getById(GetSimCardByIdCommand command) {

        // 1. Resolves the target id directly from the inbound command.
        UUID id = command.id();
        UUID userId = command.userId();

        // 2. Loads by id and fail fast when absent.
        SimCard simCard = simCardPersistence.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new SimCardNotFoundException(id));

        // 3. Maps the domain object to the response contract.
        return SimCardApplicationMapper.toResult(simCard);
    }

    /**
     * Deletes a SIM card by id.
     *
     * @param command delete-by-id payload.
     */
    @Override
    public void deleteById(DeleteSimCardByIdCommand command) {

        // 1. Resolves the target id directly from the inbound command.
        UUID id = command.id();
        java.util.UUID userId = command.userId();

        // 2. Fails fast when the SIM card does not exist for the owner.
        if (simCardPersistence.findByIdAndOwnerId(id, userId).isEmpty()) {
            throw new SimCardNotFoundException(id);
        }

        // 3. Deletes the SIM card from persistence constrained to owner.
        simCardPersistence.deleteByIdAndOwnerId(id, userId);
    }

}

