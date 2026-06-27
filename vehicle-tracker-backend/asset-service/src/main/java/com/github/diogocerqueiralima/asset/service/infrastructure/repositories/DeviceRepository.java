package com.github.diogocerqueiralima.asset.service.infrastructure.repositories;

import com.github.diogocerqueiralima.asset.service.infrastructure.entities.assets.DeviceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link DeviceEntity} instances in the database.
 */
@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, UUID> {

	/**
	 *
	 * Checks whether a device with the provided id exists and is owned by the provided owner id.
	 *
	 * @param id device identifier.
	 * @param ownerId owner identifier.
	 * @return true when the device exists and is owned by the owner, otherwise false.
	 */
	boolean existsByIdAndOwnerId(UUID id, UUID ownerId);

	/**
	 * Finds a device by id and owner id.
	 *
	 * @param id device identifier.
	 * @param ownerId owner identifier.
	 * @return device when owned by the informed owner.
	 */
	Optional<DeviceEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

	/**
	 * Retrieves a pageNumber of devices by owner id.
	 *
	 * @param ownerId owner identifier.
	 * @param pageable pageNumber request.
	 * @return paginated devices from the owner.
	 */
	Page<DeviceEntity> findAllByOwnerId(UUID ownerId, Pageable pageable);

}
