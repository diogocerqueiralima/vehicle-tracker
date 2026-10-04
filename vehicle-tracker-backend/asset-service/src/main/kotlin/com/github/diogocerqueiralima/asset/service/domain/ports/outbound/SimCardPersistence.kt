package com.github.diogocerqueiralima.asset.service.domain.ports.outbound

import com.github.diogocerqueiralima.asset.service.domain.assets.SimCard
import java.util.UUID

/**
 * Interface for SIM card persistence operations.
 * This interface defines methods for saving SIM cards to a data store and retrieving them by their ICCID.
 */
interface SimCardPersistence {

    /**
     * Saves a SIM card to the data store. If the SIM card already exists, it will be updated.
     *
     * @param simCard The SIM card to be saved or updated.
     * @return The saved or updated SIM card.
     */
    fun save(simCard: SimCard): SimCard

    /**
     * Finds a SIM card by its id.
     *
     * @param id The id of the SIM card to be retrieved.
     * @return The SIM card if found, or null if not found.
     */
    fun findById(id: UUID): SimCard?

    /**
     * Finds a SIM card by id constrained to the provided owner.
     *
     * @param id sim card identifier.
     * @param ownerId owner identifier.
     * @return matching sim card when found for the owner, otherwise null.
     */
    fun findByIdAndOwnerId(id: UUID, ownerId: UUID): SimCard?

    /**
     * Deletes a SIM card by id constrained to the provided owner.
     * Implementations should ensure the deletion only occurs when the owner matches.
     *
     * @param id sim card identifier.
     * @param ownerId owner identifier.
     */
    fun deleteByIdAndOwnerId(id: UUID, ownerId: UUID)

    /**
     * Checks whether a SIM card with the given ICCID, MSISDN or IMSI already exists.
     *
     * @param iccid ICCID to check.
     * @param msisdn MSISDN to check.
     * @param imsi IMSI to check.
     * @return true when a SIM card with any of those values already exists.
     */
    fun existsByIccidOrMsisdnOrImsi(iccid: String, msisdn: String, imsi: String): Boolean

    /**
     * Checks whether a SIM card other than the given id already uses the ICCID, MSISDN or IMSI.
     *
     * @param iccid ICCID to check.
     * @param msisdn MSISDN to check.
     * @param imsi IMSI to check.
     * @param excludingId id to exclude from the check (the SIM card being updated).
     * @return true when another SIM card with any of those values already exists.
     */
    fun isIccidOrMsisdnOrImsiTakenByAnotherSimCard(iccid: String, msisdn: String, imsi: String, excludingId: UUID): Boolean

}
