package com.github.diogocerqueiralima.application.results;

import java.util.UUID;

/**
 *
 * Result of a vehicle lookup operation.
 *
 * @param id the unique identifier of the vehicle
 * @param vin the Vehicle Identification Number
 * @param plate the vehicle's license plate
 * @param model the vehicle model
 * @param manufacturer the vehicle manufacturer
 * @param year the manufacturing year
 * @param ownerId the ID of the vehicle owner
 */
public record VehicleResult(
        UUID id, String vin, String plate, String model, String manufacturer, Integer year, UUID ownerId
) {}