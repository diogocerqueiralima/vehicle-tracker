package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.Vehicle;

import java.util.Optional;
import java.util.UUID;

/**
 * Port to interact with the vehicle data source via gRPC.
 */
public interface VehicleGrpc {

    /**
     *
     * Finds a vehicle by the ID of its associated device.
     *
     * @param id the ID of the device
     * @return an Optional containing the Vehicle if found, or empty if not found
     */
    Optional<Vehicle> findByDeviceId(UUID id);

}
