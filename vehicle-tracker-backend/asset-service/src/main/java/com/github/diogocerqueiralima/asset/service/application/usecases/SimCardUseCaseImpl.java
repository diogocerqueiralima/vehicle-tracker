package com.github.diogocerqueiralima.asset.service.application.usecases;

import com.github.diogocerqueiralima.asset.service.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.DeleteSimCardByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetSimCardByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.asset.service.application.exceptions.SimCardNotFoundException;
import com.github.diogocerqueiralima.asset.service.application.mappers.SimCardApplicationMapper;
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.SimCardUseCase;
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.SimCardPersistence;
import com.github.diogocerqueiralima.asset.service.application.results.SimCardResult;
import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 1. Creates and saves the new SIM card.
        SimCard simCardToSave = SimCardApplicationMapper.toDomain(command, Instant.now());
        SimCard savedSimCard = simCardPersistence.save(simCardToSave);

        // 5. Builds the result.
        return SimCardApplicationMapper.toResult(savedSimCard);
    }

    @Override
    @Transactional
    public SimCardResult update(UpdateSimCardCommand command) {

        UUID id = command.id();
        UUID userId = command.userId();

        // 1. Gets the SIM card with the provided id constrained to owner.
        SimCard existingSimCard = simCardPersistence.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new SimCardNotFoundException(id));

        // 2. Updates and saves the SIM card.
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
    @Transactional
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

