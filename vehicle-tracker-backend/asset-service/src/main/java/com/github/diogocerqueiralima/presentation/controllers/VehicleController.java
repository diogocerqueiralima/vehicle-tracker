package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.CreateVehicleCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetVehiclePageCommand;
import com.github.diogocerqueiralima.application.commands.UpdateVehicleCommand;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.application.results.VehicleResult;
import com.github.diogocerqueiralima.application.ports.inbound.VehicleUseCase;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.CreateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.PageDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.VehicleDTO;
import com.github.diogocerqueiralima.presentation.mappers.VehiclePresentationMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.VEHICLES_BASE_URI;
import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.VEHICLES_ID_URI;
import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.VEHICLE_PAGE_NUMBER_PARAM;
import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.VEHICLE_PAGE_SIZE_PARAM;

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
    public ResponseEntity<ApiResponseDTO<VehicleDTO>> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateVehicleRequestDTO request
    ) {

        // 1. Resolve the authenticated user id from the jwt
        UUID userId = extractUserId(jwt);

        // 2. Map transport data to an application command.
        CreateVehicleCommand command = VehiclePresentationMapper.toCreateCommand(request, userId);

        // 3. Delegate creation to the application layer.
        VehicleResult result = vehicleUseCase.create(command);

        // 4. Map the application result to the response DTO.
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
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateVehicleRequestDTO request
    ) {

        // 1. Resolve the authenticated user id from the jwt
        UUID userId = extractUserId(jwt);

        // 2. Map transport data to an application command.
        UpdateVehicleCommand command = VehiclePresentationMapper.toUpdateCommand(id, request, userId);

        // 3. Delegate update to the application layer.
        VehicleResult result = vehicleUseCase.update(command);

        // 4. Map the application result to the response DTO.
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
    public ResponseEntity<ApiResponseDTO<VehicleDTO>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {

        // 1. Resolve the authenticated user id from the jwt.
        UUID userId = extractUserId(jwt);

        // 2. Map transport data to an application command.
        GetVehicleByIdCommand command = VehiclePresentationMapper.toGetByIdCommand(id, userId);

        // 3. Delegate retrieval to the application layer.
        VehicleResult result = vehicleUseCase.getById(command);

        // 4. Map the application result to the response DTO.
        VehicleDTO vehicleDTO = VehiclePresentationMapper.toDTO(result);

        return ResponseEntity.ok(
                new ApiResponseDTO<>("Vehicle fetched successfully.", vehicleDTO)
        );
    }

    /**
     * Retrieves a one-based page of vehicles.
     *
     * @param pageNumber page number using one-based indexing.
     * @param pageSize amount of items requested per page.
     * @return paged vehicles wrapped in an API response.
     */
    @GetMapping(VEHICLES_BASE_URI)
    public ResponseEntity<ApiResponseDTO<PageDTO<VehicleDTO>>> getPage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(VEHICLE_PAGE_NUMBER_PARAM) int pageNumber,
            @RequestParam(VEHICLE_PAGE_SIZE_PARAM) int pageSize
    ) {

        // 1. Resolve the authenticated user id from the jwt
        UUID userId = extractUserId(jwt);

        // 2. Maps query params to application command.
        GetVehiclePageCommand command = VehiclePresentationMapper.toGetPageCommand(pageNumber, pageSize, userId);

        // 3. Delegates retrieval of the page to the application layer.
        PageResult<VehicleResult> result = vehicleUseCase.getPage(command);

        // 4. Converts application result to transport DTO.
        PageDTO<VehicleDTO> pageDTO = VehiclePresentationMapper.toPageDTO(result);

        return ResponseEntity.ok(
                new ApiResponseDTO<>("Vehicles fetched successfully.", pageDTO)
        );
    }

    private UUID extractUserId(Jwt jwt) {

        // 1. Keycloak stores the user id in the token subject claim.
        String subject = jwt.getSubject();

        // 2. Converts subject to UUID used by application/domain contracts.
        return UUID.fromString(subject);
    }

}

