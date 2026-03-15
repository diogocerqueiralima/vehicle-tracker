package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.assignments.SimCardAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link SimCardAssignmentEntity} instances in the database.
 */
@Repository
public interface SimCardAssignmentRepository extends JpaRepository<SimCardAssignmentEntity, Long> {}