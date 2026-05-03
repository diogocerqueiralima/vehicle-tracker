package com.github.diogocerqueiralima.application.ports.outbound;

import com.github.diogocerqueiralima.domain.SimCard;

import java.util.Optional;

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
     * Finds a SIM card by its ICCID.
     *
     * @param iccid The ICCID of the SIM card to be retrieved.
     * @return An Optional containing the SIM card if found, or an empty Optional if not found.
     */
    Optional<SimCard> findByIccid(String iccid);

    /**
     *
     * Checks whether a SIM card with the provided ICCID already exists.
     *
     * @param iccid The ICCID to search for.
     * @return true if a SIM card with the ICCID exists, otherwise false.
     */
    boolean existsByIccid(String iccid);

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
     * Deletes a SIM card from the data store by its ICCID.
     *
     * @param iccid the ICCID of the SIM card to be deleted.
     */
    void deleteByIccid(String iccid);

}