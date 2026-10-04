package com.github.diogocerqueiralima.asset.service.application.mappers

import com.github.diogocerqueiralima.asset.service.application.commands.CreateVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.application.results.VehicleResult
import com.github.diogocerqueiralima.asset.service.domain.assets.Vehicle
import org.springframework.data.domain.Page
import java.time.Instant
import java.util.UUID

/**
 * Mapper for vehicle conversions in the application layer.
 */
object VehicleApplicationMapper {

    /**
     * Builds a domain vehicle from a create command and the current timestamp.
     *
     * @param command create command with the vehicle data.
     * @param now current timestamp for createdAt and updatedAt fields.
     * @return new domain vehicle with the provided data and timestamps.
     */
    fun toDomain(command: CreateVehicleCommand, now: Instant): Vehicle =
        Vehicle(
            UUID.randomUUID(),
            command.userId,
            now,
            now,
            command.vin,
            command.plate,
            command.model,
            command.manufacturer,
            command.manufacturingDate
        )

    /**
     * Builds a domain vehicle from an update command and the existing vehicle.
     *
     * @param command update command with the new vehicle data.
     * @param existingVehicle existing vehicle to be updated.
     * @param updatedAt timestamp of the update operation.
     * @return updated domain vehicle with the new data and timestamps.
     */
    fun toDomain(command: UpdateVehicleCommand, existingVehicle: Vehicle, updatedAt: Instant): Vehicle =
        Vehicle(
            existingVehicle.id,
            existingVehicle.ownerId,
            existingVehicle.createdAt,
            updatedAt,
            command.vin,
            command.plate,
            command.model,
            command.manufacturer,
            command.manufacturingDate
        )

    /**
     * Builds a vehicle application result from a domain vehicle.
     *
     * @param vehicle domain vehicle.
     * @return vehicle application result.
     */
    fun toResult(vehicle: Vehicle): VehicleResult =
        VehicleResult(
            vehicle.id,
            vehicle.createdAt,
            vehicle.updatedAt,
            vehicle.vin,
            vehicle.plate,
            vehicle.model,
            vehicle.manufacturer,
            vehicle.manufacturingDate
        )

    /**
     * Converts a paginated domain payload into an application result payload.
     *
     * @param vehiclePageResult paginated domain vehicles.
     * @return paginated vehicle application result.
     */
    fun toPageResult(vehiclePageResult: Page<Vehicle>): PageResult<VehicleResult> =
        PageResult(
            vehiclePageResult.number + 1,
            vehiclePageResult.size,
            vehiclePageResult.totalPages,
            vehiclePageResult.totalElements,
            vehiclePageResult.map { toResult(it) }.toList()
        )

}
