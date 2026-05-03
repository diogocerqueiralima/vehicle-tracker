package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.assets.SimCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for managing {@link SimCardEntity} instances in the database.
 */
@Repository
public interface SimCardRepository extends JpaRepository<SimCardEntity, UUID> {

	boolean existsByIccid(String iccid);

	void deleteById(UUID id);

	boolean existsByMsisdn(String msisdn);

	boolean existsByImsi(String imsi);

}
