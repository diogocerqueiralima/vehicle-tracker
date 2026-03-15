package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.ports.inbound.VehicleUseCase;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.CreateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.VehicleDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

        CreateVehicleCommand command = new CreateVehicleCommand(
                request.vin(),
                request.plate(),
                request.model(),
                request.manufacturer(),
                request.manufacturingDate()
        );

        VehicleResult result = vehicleUseCase.create(command);

        VehicleDTO vehicleDTO = new VehicleDTO(
                result.id(),
                result.createdAt(),
                result.updatedAt(),
                result.vin(),
                result.plate(),
                result.model(),
                result.manufacturer(),
                result.manufacturingDate()
        );

        ApiResponseDTO<VehicleDTO> response = new ApiResponseDTO<>("Vehicle created successfully.", vehicleDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}

