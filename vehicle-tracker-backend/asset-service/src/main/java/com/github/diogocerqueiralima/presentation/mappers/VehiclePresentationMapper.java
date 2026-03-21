package com.github.diogocerqueiralima.presentation.mappers;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleByIdCommand;
import com.github.diogocerqueiralima.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.presentation.dto.CreateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.VehicleDTO;

import java.util.UUID;

/**
 * Mapper for vehicle conversions in the presentation layer.
 */
public final class VehiclePresentationMapper {

    private VehiclePresentationMapper() {}

    public static CreateVehicleCommand toCreateCommand(CreateVehicleRequestDTO request) {
        return new CreateVehicleCommand(
                request.vin(),
                request.plate(),
                request.model(),
                request.manufacturer(),
                request.manufacturingDate()
        );
    }

    public static UpdateVehicleCommand toUpdateCommand(UUID id, UpdateVehicleRequestDTO request) {
        return new UpdateVehicleCommand(
                id,
                request.vin(),
                request.plate(),
                request.model(),
                request.manufacturer(),
                request.manufacturingDate()
        );
    }

    /**
     * Builds a get-by-id command from an HTTP path identifier.
     *
     * @param id vehicle identifier from the request path.
     * @return command consumed by the application layer.
     */
    public static GetVehicleByIdCommand toGetByIdCommand(UUID id) {
        return new GetVehicleByIdCommand(id);
    }

    public static VehicleDTO toDTO(VehicleResult result) {
        return new VehicleDTO(
                result.id(),
                result.createdAt(),
                result.updatedAt(),
                result.vin(),
                result.plate(),
                result.model(),
                result.manufacturer(),
                result.manufacturingDate()
        );
    }

}

