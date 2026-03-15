package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.assignments.VehicleAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link VehicleAssignmentEntity} instances in the database.
 */
@Repository
public interface VehicleAssignmentRepository extends JpaRepository<VehicleAssignmentEntity, Long> {}