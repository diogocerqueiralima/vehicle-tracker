package com.github.diogocerqueiralima.asset.service.application.usecases

import com.github.diogocerqueiralima.error.common.exceptions.ConflictException
import com.github.diogocerqueiralima.error.common.exceptions.NotFoundException
import com.github.diogocerqueiralima.asset.service.application.commands.CreateVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehicleByIdCommand
import com.github.diogocerqueiralima.asset.service.application.commands.GetVehiclePageCommand
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateVehicleCommand
import com.github.diogocerqueiralima.asset.service.application.mappers.VehicleApplicationMapper
import com.github.diogocerqueiralima.asset.service.application.results.PageResult
import com.github.diogocerqueiralima.asset.service.application.results.VehicleResult
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.VehicleUseCase
import com.github.diogocerqueiralima.asset.service.domain.ports.outbound.VehiclePersistence
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Application-layer implementation that orchestrates vehicle use cases.
 */
@Service
class VehicleUseCaseImpl(private val vehiclePersistence: VehiclePersistence) : VehicleUseCase {

    override fun create(command: CreateVehicleCommand): VehicleResult {

        // 1. Fails when the VIN or plate is already in use.
        if (vehiclePersistence.existsByVinOrPlate(command.vin, command.plate)) {
            throw ConflictException("A vehicle with the provided VIN or plate already exists.")
        }

        // 2. Create a new vehicle
        val vehicle = VehicleApplicationMapper.toDomain(command, Instant.now())

        // 3. Saves the vehicle
        val savedVehicle = vehiclePersistence.save(vehicle)

        // 4. Build the result
        return VehicleApplicationMapper.toResult(savedVehicle)
    }

    @Transactional
    override fun update(command: UpdateVehicleCommand): VehicleResult {

        val id = command.id

        // 1. Gets the vehicle by id constrained to the authenticated owner.
        val existingVehicle = vehiclePersistence.findByIdAndOwnerId(id, command.userId)
            ?: throw NotFoundException("Vehicle not found for id: $id")

        // 2. Fails when another vehicle already uses the VIN or plate.
        if (vehiclePersistence.isVinOrPlateTakenByAnotherVehicle(command.vin, command.plate, id)) {
            throw ConflictException("A vehicle with the provided VIN or plate already exists.")
        }

        // 3. Update the vehicle
        val vehicleToSave = VehicleApplicationMapper.toDomain(command, existingVehicle, Instant.now())

        // 4. Save the vehicle
        val updatedVehicle = vehiclePersistence.save(vehicleToSave)

        // 5. Build the result
        return VehicleApplicationMapper.toResult(updatedVehicle)
    }

    /**
     * Retrieves a vehicle by id.
     *
     * @param command get-by-id payload.
     * @return the matching vehicle as a result object.
     */
    override fun getById(command: GetVehicleByIdCommand): VehicleResult {

        // 1. Load by id constrained to owner and fail fast when absent.
        val vehicle = vehiclePersistence.findByIdAndOwnerId(command.id, command.userId)
            ?: throw NotFoundException("Vehicle not found for id: ${command.id}")

        // 2. Map the domain object to the response contract.
        return VehicleApplicationMapper.toResult(vehicle)
    }

    /**
     * Retrieves a one-based page of vehicles.
     *
     * @param command page request payload.
     * @return paginated vehicle result.
     */
    override fun getPage(command: GetVehiclePageCommand): PageResult<VehicleResult> {

        // 1. Fetches the owner-scoped page from persistence preserving one-based indexing semantics.
        val vehiclePageResult = vehiclePersistence.getPageByOwnerId(command.pageNumber - 1, command.pageSize, command.userId)

        // 2. Converts domain page payload to application output contract.
        return VehicleApplicationMapper.toPageResult(vehiclePageResult)
    }

}
