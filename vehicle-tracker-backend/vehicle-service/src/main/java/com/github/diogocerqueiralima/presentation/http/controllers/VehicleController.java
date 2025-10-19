package com.github.diogocerqueiralima.presentation.http.controllers;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.domain.ports.inbound.VehicleService;
import com.github.diogocerqueiralima.presentation.http.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.http.dto.CreateVehicleDTO;
import com.github.diogocerqueiralima.presentation.http.dto.VehicleDTO;
import com.github.diogocerqueiralima.presentation.mappers.VehicleMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.github.diogocerqueiralima.presentation.http.Paths.*;

/**
 * Rest controller for managing vehicle-related HTTP requests.
 */
@RestController
@RequestMapping(VEHICLE_PATH)
public class VehicleController {

    private final VehicleMapper vehicleMapper;
    private final VehicleService vehicleService;

    public VehicleController(@Qualifier("presentation") VehicleMapper vehicleMapper, VehicleService vehicleService) {
        this.vehicleMapper = vehicleMapper;
        this.vehicleService = vehicleService;
    }

    /**
     *
     * Creates a new vehicle.
     *
     * @param dto the DTO containing vehicle creation details
     * @param jwt the JWT token of the authenticated user
     * @return a ResponseEntity containing the created vehicle DTO and a success message
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<VehicleDTO>> create(
            @RequestBody CreateVehicleDTO dto, @AuthenticationPrincipal Jwt jwt
    ) {

        VehicleResult result = vehicleService.create(
                new CreateVehicleCommand(
                        dto.vin(),
                        dto.plate(),
                        dto.model(),
                        dto.manufacturer(),
                        dto.year(),
                        UUID.fromString(jwt.getSubject())
                )
        );

        VehicleDTO resultDTO = vehicleMapper.toDTO(result);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.of(resultDTO, "Vehicle created successfully"));
    }

}
