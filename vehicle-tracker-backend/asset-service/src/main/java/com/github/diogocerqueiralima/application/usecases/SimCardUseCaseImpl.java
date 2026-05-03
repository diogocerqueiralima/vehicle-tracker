package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.DeleteSimCardByIccidCommand;
import com.github.diogocerqueiralima.application.commands.GetSimCardByIccidCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.application.exceptions.SimCardAlreadyExistsException;
import com.github.diogocerqueiralima.application.exceptions.SimCardNotFoundException;
import com.github.diogocerqueiralima.application.mappers.SimCardApplicationMapper;
import com.github.diogocerqueiralima.application.ports.inbound.SimCardUseCase;
import com.github.diogocerqueiralima.application.ports.outbound.SimCardPersistence;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.domain.SimCard;
import org.springframework.stereotype.Service;

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
        SimCard simCardToSave = SimCardApplicationMapper.toDomain(command);
        SimCard savedSimCard = simCardPersistence.save(simCardToSave);

        // 5. Builds the result.
        return SimCardApplicationMapper.toResult(savedSimCard);
    }

    @Override
    public SimCardResult update(UpdateSimCardCommand command) {

        String iccid = command.iccid();

        // 1. Gets the SIM card with the provided ICCID.
        SimCard existingSimCard = simCardPersistence.findByIccid(iccid)
                .orElseThrow(() -> new SimCardNotFoundException(iccid));

        // 2. Checks if exists another SIM card with the given MSISDN.
        if (!existingSimCard.getMsisdn().equals(command.msisdn())
                && simCardPersistence.existsByMsisdn(command.msisdn())) {
            throw new SimCardAlreadyExistsException("A SIM card with the provided MSISDN already exists.");
        }

        // 3. Checks if exists another SIM card with the given IMSI.
        if (!existingSimCard.getImsi().equals(command.imsi())
                && simCardPersistence.existsByImsi(command.imsi())) {
            throw new SimCardAlreadyExistsException("A SIM card with the provided IMSI already exists.");
        }

        // 4. Updates and saves the SIM card.
        SimCard simCardToSave = SimCardApplicationMapper.toDomain(command);
        SimCard updatedSimCard = simCardPersistence.save(simCardToSave);

        // 5. Builds the result.
        return SimCardApplicationMapper.toResult(updatedSimCard);
    }

    /**
     * Retrieves a SIM card by ICCID.
     *
     * @param command get-by-iccid payload.
     * @return the matching SIM card as a result object.
     */
    @Override
    public SimCardResult getByIccid(GetSimCardByIccidCommand command) {

        // 1. Resolves the target ICCID directly from the inbound command.
        String iccid = command.iccid();

        // 2. Loads by ICCID and fail fast when absent.
        SimCard simCard = simCardPersistence.findByIccid(iccid)
                .orElseThrow(() -> new SimCardNotFoundException(iccid));

        // 3. Maps the domain object to the response contract.
        return SimCardApplicationMapper.toResult(simCard);
    }

    /**
     * Deletes a SIM card by ICCID.
     *
     * @param command delete-by-iccid payload.
     */
    @Override
    public void deleteByIccid(DeleteSimCardByIccidCommand command) {

        // 1. Resolves the target ICCID directly from the inbound command.
        String iccid = command.iccid();

        // 2. Fails fast when the SIM card does not exist.
        if (!simCardPersistence.existsByIccid(iccid)) {
            throw new SimCardNotFoundException(iccid);
        }

        // 3. Deletes the SIM card from persistence.
        simCardPersistence.deleteByIccid(iccid);
    }

}

