package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.assets.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for managing {@link VehicleEntity} instances in the database.
 */
@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, UUID> {}