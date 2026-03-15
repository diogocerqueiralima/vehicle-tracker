package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for managing {@link DeviceEntity} instances in the database.
 */
@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, UUID> {}