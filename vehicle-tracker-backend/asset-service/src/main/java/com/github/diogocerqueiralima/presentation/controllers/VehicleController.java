package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleByIdCommand;
import com.github.diogocerqueiralima.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.application.ports.inbound.VehicleUseCase;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.CreateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.VehicleDTO;
import com.github.diogocerqueiralima.presentation.mappers.VehiclePresentationMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.VEHICLES_BASE_URI;
import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.VEHICLES_ID_URI;

/**
 * REST endpoints for vehicle operations.
 */
@RestController
public class VehicleController {

    private final VehicleUseCase vehicleUseCase;

    public VehicleController(VehicleUseCase vehicleUseCase) {
        this.vehicleUseCase = vehicleUseCase;
    }

    /**
     * Creates a new vehicle.
     *
     * @param request request payload for vehicle creation.
     * @return created vehicle wrapped in an API response.
     */
    @PostMapping(VEHICLES_BASE_URI)
    public ResponseEntity<ApiResponseDTO<VehicleDTO>> create(@Valid @RequestBody CreateVehicleRequestDTO request) {

        // 1. Map transport data to an application command.
        CreateVehicleCommand command = VehiclePresentationMapper.toCreateCommand(request);

        // 2. Delegate creation to the application layer.
        VehicleResult result = vehicleUseCase.create(command);

        // 3. Map the application result to the response DTO.
        VehicleDTO vehicleDTO = VehiclePresentationMapper.toDTO(result);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>("Vehicle created successfully.", vehicleDTO));
    }

    /**
     * Updates an existing vehicle.
     *
     * @param id vehicle identifier.
     * @param request request payload for vehicle update.
     * @return updated vehicle wrapped in an API response.
     */
    @PutMapping(VEHICLES_ID_URI)
    public ResponseEntity<ApiResponseDTO<VehicleDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleRequestDTO request
    ) {

        // 1. Map transport data to an application command.
        UpdateVehicleCommand command = VehiclePresentationMapper.toUpdateCommand(id, request);

        // 2. Delegate update to the application layer.
        VehicleResult result = vehicleUseCase.update(command);

        // 3. Map the application result to the response DTO.
        VehicleDTO vehicleDTO = VehiclePresentationMapper.toDTO(result);

        return ResponseEntity.ok(
                new ApiResponseDTO<>("Vehicle updated successfully.", vehicleDTO)
        );
    }

    /**
     * Retrieves a vehicle by id.
     *
     * @param id vehicle identifier.
     * @return vehicle wrapped in an API response.
     */
    @GetMapping(VEHICLES_ID_URI)
    public ResponseEntity<ApiResponseDTO<VehicleDTO>> getById(@PathVariable UUID id) {

        // 1. Map transport data to an application command.
        GetVehicleByIdCommand command = VehiclePresentationMapper.toGetByIdCommand(id);

        // 2. Delegate retrieval to the application layer.
        VehicleResult result = vehicleUseCase.getById(command);

        // 3. Map the application result to the response DTO.
        VehicleDTO vehicleDTO = VehiclePresentationMapper.toDTO(result);

        return ResponseEntity.ok(
                new ApiResponseDTO<>("Vehicle fetched successfully.", vehicleDTO)
        );
    }

}

