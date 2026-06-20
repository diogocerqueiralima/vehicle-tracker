package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.assignments.VehicleAssignmentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link VehicleAssignmentEntity} instances in the database.
 */
@Repository
public interface VehicleAssignmentRepository extends JpaRepository<VehicleAssignmentEntity, Long> {

	/**
	 * Finds an active assignment for a specific device and vehicle pair, acquiring a pessimistic write lock
	 * to prevent concurrent unassign operations from producing lost audit data.
	 *
	 * @param deviceId device unique identifier.
	 * @param vehicleId vehicle unique identifier.
	 * @return active assignment when found.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<VehicleAssignmentEntity> findByDeviceIdAndVehicleIdAndUnassignedAtIsNull(UUID deviceId, UUID vehicleId);

	/**
	 *
	 * Finds an active assignment for a specific device.
	 *
	 * @param deviceId device unique identifier.
	 * @return active assignment when found, otherwise empty.
	 */
	Optional<VehicleAssignmentEntity> findByDeviceIdAndUnassignedAtIsNull(UUID deviceId);

	/**
	 *
	 * Finds the history of assignments for a specific vehicle and user, returning a paginated result.
	 *
	 * @param vehicleId The unique identifier of the vehicle for which to retrieve the assignment history.
	 * @param userId The unique identifier of the user who owns the vehicle.
	 * @param pageable The pagination information, including pageNumber number and pageNumber size, to control the amount of data returned in each pageNumber.
	 * @return A paginated list of {@link VehicleAssignmentEntity} instances representing the assignment history of the specified vehicle for the given user.
	 */
	@Query(
			value = """
					SELECT va.*
					FROM vehicle_assignments va
					JOIN assets a ON va.vehicle_id = a.id
					WHERE a.owner_id = :userId
					AND va.vehicle_id = :vehicleId
					""",
			nativeQuery = true
	)
	Page<VehicleAssignmentEntity> findHistory(UUID vehicleId, UUID userId, Pageable pageable);

}
