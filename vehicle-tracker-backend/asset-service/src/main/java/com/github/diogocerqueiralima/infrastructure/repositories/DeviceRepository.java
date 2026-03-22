package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}
