package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.VEHICLES_BASE_URI;

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

        CreateVehicleCommand command = VehiclePresentationMapper.toCreateCommand(request);

        VehicleResult result = vehicleUseCase.create(command);

        VehicleDTO vehicleDTO = VehiclePresentationMapper.toDTO(result);

        ApiResponseDTO<VehicleDTO> response = new ApiResponseDTO<>("Vehicle created successfully.", vehicleDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing vehicle.
     *
     * @param id vehicle identifier.
     * @param request request payload for vehicle update.
     * @return updated vehicle wrapped in an API response.
     */
    @PutMapping(VEHICLES_BASE_URI + "/{id}")
    public ResponseEntity<ApiResponseDTO<VehicleDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleRequestDTO request
    ) {

        UpdateVehicleCommand command = VehiclePresentationMapper.toUpdateCommand(id, request);

        VehicleResult result = vehicleUseCase.update(command);

        VehicleDTO vehicleDTO = VehiclePresentationMapper.toDTO(result);

        return ResponseEntity.ok(
                new ApiResponseDTO<>("Vehicle updated successfully.", vehicleDTO)
        );
    }

}

