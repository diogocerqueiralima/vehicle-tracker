package com.github.diogocerqueiralima.infrastructure.repositories;

import com.github.diogocerqueiralima.infrastructure.entities.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, UUID> {

    /**
     *
     * Finds a vehicle by its associated device ID.
     *
     * @param deviceId the UUID of the device
     * @return an Optional containing the VehicleEntity if found, or empty if not found
     */
    Optional<VehicleEntity> findByDeviceId(UUID deviceId);

    /**
     *
     * Checks if a vehicle with the given VIN exists.
     *
     * @param vin the vehicle identification number to check
     * @return true if a vehicle with the given VIN exists, false otherwise
     */
    boolean existsByVin(String vin);

    /**
     *
     * Checks if a vehicle with the given plate exists.
     *
     * @param plate the license plate to check
     * @return true if a vehicle with the given plate exists, false otherwise
     */
    boolean existsByPlate(String plate);

}
