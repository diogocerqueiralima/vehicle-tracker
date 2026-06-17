package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.assignments.VehicleAssignmentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link VehicleAssignmentEntity} instances in the database.
 */
@Repository
public interface VehicleAssignmentRepository extends JpaRepository<VehicleAssignmentEntity, Long> {

	/**
	 * Checks whether there is an active assignment for a given device.
	 *
	 * @param deviceId device unique identifier.
	 * @return true when an active assignment exists for the device.
	 */
	boolean existsByDeviceIdAndUnassignedAtIsNull(UUID deviceId);

	/**
	 * Checks whether there is an active assignment for a given vehicle.
	 *
	 * @param vehicleId vehicle unique identifier.
	 * @return true when an active assignment exists for the vehicle.
	 */
	boolean existsByVehicleIdAndUnassignedAtIsNull(UUID vehicleId);

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

}
