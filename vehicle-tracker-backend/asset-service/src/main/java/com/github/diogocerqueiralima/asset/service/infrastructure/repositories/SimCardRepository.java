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

}
