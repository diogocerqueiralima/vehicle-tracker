package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
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
	 * Checks whether a device with the provided serial number exists.
	 *
	 * @param serialNumber the serial number to lookup.
	 * @return true when a record with the serial number exists.
	 */
	boolean existsBySerialNumber(String serialNumber);

	/**
	 * Checks whether a device with the provided IMEI exists.
	 *
	 * @param imei the IMEI to lookup.
	 * @return true when a record with the IMEI exists.
	 */
	boolean existsByImei(String imei);

	/**
	 * Finds a device by id and owner id.
	 *
	 * @param id device identifier.
	 * @param ownerId owner identifier.
	 * @return device when owned by the informed owner.
	 */
	Optional<DeviceEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

	/**
	 * Retrieves a page of devices by owner id.
	 *
	 * @param ownerId owner identifier.
	 * @param pageable page request.
	 * @return paginated devices from the owner.
	 */
	Page<DeviceEntity> findAllByOwnerId(UUID ownerId, Pageable pageable);

}
