package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.assignments.SimCardAssignmentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link SimCardAssignmentEntity} instances in the database.
 */
@Repository
public interface SimCardAssignmentRepository extends JpaRepository<SimCardAssignmentEntity, Long> {

	/**
	 * Checks whether there is an active assignment for a given device.
	 *
	 * @param deviceId device unique identifier.
	 * @return true when an active assignment exists for the device.
	 */
	boolean existsByDeviceIdAndUnassignedAtIsNull(UUID deviceId);

	/**
	 * Checks whether there is an active assignment for a given SIM card.
	 *
	 * @param id SIM card id.
	 * @return true when an active assignment exists for the SIM card.
	 */
	boolean existsBySimCardIdAndUnassignedAtIsNull(UUID id);

	/**
	 * Finds an active assignment for a specific device and SIM card pair, acquiring a pessimistic write lock
	 * to prevent concurrent unassign operations from producing lost audit data.
	 *
	 * @param deviceId device unique identifier.
	 * @param simCardId SIM card id.
	 * @return active assignment when found.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<SimCardAssignmentEntity> findByDeviceIdAndSimCardIdAndUnassignedAtIsNull(UUID deviceId, UUID simCardId);

}
