package com.github.diogocerqueiralima.asset.service.infrastructure.repositories;

import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.SimCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link SimCardEntity} instances in the database.
 */
@Repository
public interface SimCardRepository extends JpaRepository<SimCardEntity, UUID> {

    Optional<SimCardEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

	void deleteByIdAndOwnerId(UUID id, UUID ownerId);

	/**
	 * Checks whether a SIM card with the given ICCID, MSISDN or IMSI already exists.
	 *
	 * @param iccid ICCID to check.
	 * @param msisdn MSISDN to check.
	 * @param imsi IMSI to check.
	 * @return true when a SIM card with any of those values already exists.
	 */
	boolean existsByIccidOrMsisdnOrImsi(String iccid, String msisdn, String imsi);

	/**
	 * Checks whether a SIM card other than the given id already uses the ICCID, MSISDN or IMSI.
	 *
	 * @param iccid ICCID to check.
	 * @param msisdn MSISDN to check.
	 * @param imsi IMSI to check.
	 * @param id id to exclude from the check.
	 * @return true when another SIM card with any of those values already exists.
	 */
	boolean existsByIccidOrMsisdnOrImsiAndIdNot(String iccid, String msisdn, String imsi, UUID id);

}
