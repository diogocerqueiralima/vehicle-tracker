package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.SimCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link SimCardEntity} instances in the database.
 */
@Repository
public interface SimCardRepository extends JpaRepository<SimCardEntity, String> {}