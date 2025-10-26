package com.github.diogocerqueiralima.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    @Test
    public void checks_if_vehicle_belongs_to_owner_should_succeed() {

        UUID ownerId = UUID.randomUUID();
        Vehicle vehicle = new Vehicle(
                "1HGCM82633A123456",
                "AB-12-C3",
                "Civic",
                "Honda",
                2020,
                ownerId
        );

        assertTrue(vehicle.isOwnedBy(ownerId));
        assertFalse(vehicle.isOwnedBy(UUID.randomUUID()));
    }

}