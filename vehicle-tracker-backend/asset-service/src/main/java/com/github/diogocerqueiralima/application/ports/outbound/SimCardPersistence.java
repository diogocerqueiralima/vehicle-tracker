package com.github.diogocerqueiralima.application.ports.outbound;

import com.github.diogocerqueiralima.domain.assets.SimCard;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for SIM card persistence operations.
 * This interface defines methods for saving SIM cards to a data store and retrieving them by their ICCID.
 */
public interface SimCardPersistence {

    /**
     *
     * Saves a SIM card to the data store. If the SIM card already exists, it will be updated.
     *
     * @param simCard The SIM card to be saved or updated.
     * @return The saved or updated SIM card.
     */
    SimCard save(SimCard simCard);

    /**
     *
     * Finds a SIM card by its id.
     *
     * @param id The id of the SIM card to be retrieved.
     * @return An Optional containing the SIM card if found, or an empty Optional if not found.
     */
    Optional<SimCard> findById(UUID id);

    /**
     * Finds a SIM card by id constrained to the provided owner.
     *
     * @param id sim card identifier.
     * @param ownerId owner identifier.
     * @return matching sim card when found for the owner.
     */
    Optional<SimCard> findByIdAndOwnerId(UUID id, UUID ownerId);

    /**
     *
     * Checks whether a SIM card with the provided MSISDN already exists.
     *
     * @param msisdn The MSISDN to search for.
     * @return true if a SIM card with the MSISDN exists, otherwise false.
     */
    boolean existsByMsisdn(String msisdn);

    /**
     *
     * Checks whether a SIM card with the provided IMSI already exists.
     *
     * @param imsi The IMSI to search for.
     * @return true if a SIM card with the IMSI exists, otherwise false.
     */
    boolean existsByImsi(String imsi);

    /**
     *
     * Checks whether a SIM card with the provided ICCID already exists.
     *
     * @param iccid The ICCID to search for.
     * @return true if a SIM card with the ICCID exists, otherwise false.
     */
    boolean existsByIccid(String iccid);

    /**
     * Deletes a SIM card by id constrained to the provided owner.
     * Implementations should ensure the deletion only occurs when the owner matches.
     *
     * @param id sim card identifier.
     * @param ownerId owner identifier.
     */
    void deleteByIdAndOwnerId(UUID id, UUID ownerId);

}