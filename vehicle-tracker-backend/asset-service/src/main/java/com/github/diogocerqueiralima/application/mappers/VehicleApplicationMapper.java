package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.assets.Vehicle;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.UUID;

/**
 * Mapper for vehicle conversions in the application layer.
 */
public final class VehicleApplicationMapper {

    // Should not be instantiated
    private VehicleApplicationMapper() {}

    /**
     *
     * Builds a domain vehicle from a create command and the current timestamp.
     *
     * @param command create command with the vehicle data.
     * @param now current timestamp for createdAt and updatedAt fields.
     * @return new domain vehicle with the provided data and timestamps.
     */
    public static Vehicle toDomain(CreateVehicleCommand command, Instant now) {
        return new Vehicle(
                UUID.randomUUID(),
                now,
                now,
                command.vin(),
                command.plate(),
                command.model(),
                command.manufacturer(),
                command.manufacturingDate()
        );
    }

    /**
     *
     * Builds a domain vehicle from an update command and the existing vehicle.
     *
     * @param command update command with the new vehicle data.
     * @param existingVehicle existing vehicle to be updated.
     * @param updatedAt timestamp of the update operation.
     * @return updated domain vehicle with the new data and timestamps.
     */
    public static Vehicle toDomain(UpdateVehicleCommand command, Vehicle existingVehicle, Instant updatedAt) {
        return new Vehicle(
                existingVehicle.getId(),
                existingVehicle.getCreatedAt(),
                updatedAt,
                command.vin(),
                command.plate(),
                command.model(),
                command.manufacturer(),
                command.manufacturingDate()
        );
    }

    /**
     *
     * Builds a vehicle application result from a domain vehicle.
     *
     * @param vehicle domain vehicle.
     * @return vehicle application result.
     */
    public static VehicleResult toResult(Vehicle vehicle) {
        return new VehicleResult(
                vehicle.getId(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt(),
                vehicle.getVin(),
                vehicle.getPlate(),
                vehicle.getModel(),
                vehicle.getManufacturer(),
                vehicle.getManufacturingDate()
        );
    }

    /**
     * Converts a paginated domain payload into an application result payload.
     *
     * @param vehiclePageResult paginated domain vehicles.
     * @return paginated vehicle application result.
     */
    public static PageResult<VehicleResult> toPageResult(Page<Vehicle> vehiclePageResult) {
        return new PageResult<>(
                vehiclePageResult.getNumber() + 1,
                vehiclePageResult.getSize(),
                vehiclePageResult.getTotalElements(),
                vehiclePageResult.getTotalPages(),
                vehiclePageResult.stream()
                        .map(VehicleApplicationMapper::toResult)
                        .toList()
        );
    }

}

