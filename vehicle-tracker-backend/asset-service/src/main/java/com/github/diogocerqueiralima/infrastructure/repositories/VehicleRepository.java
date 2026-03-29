package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.assets.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for managing {@link VehicleEntity} instances in the database.
 */
@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, UUID> {

	/**
	 * Checks whether a vehicle with the provided VIN exists.
	 *
	 * @param vin the VIN to lookup.
	 * @return true when a record with the VIN exists.
	 */
	boolean existsByVin(String vin);

	/**
	 * Checks whether a vehicle with the provided plate exists.
	 *
	 * @param plate the plate to lookup.
	 * @return true when a record with the plate exists.
	 */
	boolean existsByPlate(String plate);

}
