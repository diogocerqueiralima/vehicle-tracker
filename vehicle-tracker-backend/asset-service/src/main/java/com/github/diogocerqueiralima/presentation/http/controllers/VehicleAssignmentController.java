package com.github.diogocerqueiralima.presentation.http.controllers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.application.commands.GetVehicleAssignmentHistoryCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.domain.ports.inbound.VehicleAssignmentUseCase;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.presentation.http.dto.*;
import com.github.diogocerqueiralima.presentation.http.mappers.VehicleAssignmentHttpMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.github.diogocerqueiralima.presentation.http.config.ApplicationURIs.*;

/**
 * REST endpoints for vehicle assignment operations.
 */
@Tag(name = "Vehicle Assignments", description = "Operations related to vehicle assignments, including assigning and unassigning devices to vehicles.")
@SecurityRequirements(value = { @SecurityRequirement(name = "bearerAuth") })
@RestController
public class VehicleAssignmentController {

    private final VehicleAssignmentUseCase vehicleAssignmentUseCase;

    public VehicleAssignmentController(VehicleAssignmentUseCase vehicleAssignmentUseCase) {
        this.vehicleAssignmentUseCase = vehicleAssignmentUseCase;
    }

    /**
     * Assigns a device to a vehicle.
     *
     * @param request request payload for assignment.
     * @return created assignment wrapped in an API response.
     */
    @Operation(
            summary = "Assigns a device to a vehicle.",
            description = """
                    Accepts a request payload containing the device identifier, assigns the device to the specified vehicle,
                    and returns the created assignment information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Successfully assigned the device to the vehicle"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The assignment failed or the request payload is invalid"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The vehicle or device with the specified ID was not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the assignment request"
                    )
            }
    )
    @PostMapping(VEHICLES_ASSIGNMENTS_BASE_URI)
    public ResponseEntity<ApiResponseDTO<VehicleAssignmentDTO>> assignDeviceToVehicle(
            JwtAuthenticationToken authentication,
            @PathVariable UUID vehicleId,
            @RequestBody AssignDeviceToVehicleRequestDTO request
    ) {

        // 1. Resolve the authenticated user id from the jwt.
        UUID assignedBy = extractUserId(authentication);

        // 2. Maps transport data to an application command.
        AssignDeviceToVehicleCommand command = VehicleAssignmentHttpMapper.toAssignDeviceToVehicleCommand(
                request, vehicleId, assignedBy
        );

        // 3. Delegates assignment creation to the application layer.
        VehicleAssignmentResult result = vehicleAssignmentUseCase.assignDeviceToVehicle(command);

        // 4. Converts application output into the response payload.
        VehicleAssignmentDTO responseData = VehicleAssignmentHttpMapper.toDTO(result);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>("Device assigned to vehicle successfully.", responseData));
    }

    /**
     * Unassigns a device from a vehicle.
     *
     * @param request request payload for unassignment.
     * @return updated assignment wrapped in an API response.
     */
    @Operation(
            summary = "Unassigns a device from a vehicle.",
            description = """
                    Accepts a request payload containing the device identifier, closes the active assignment between the device
                    and the specified vehicle, and returns the updated assignment information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully unassigned the device from the vehicle"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The unassignment failed or the request payload is invalid"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The vehicle or active assignment with the specified ID was not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the unassignment request"
                    )
            }
    )
    @DeleteMapping(VEHICLES_ASSIGNMENTS_BASE_URI)
    public ResponseEntity<ApiResponseDTO<VehicleAssignmentDTO>> unassignDeviceFromVehicle(
            JwtAuthenticationToken authentication,
            @PathVariable UUID vehicleId,
            @RequestBody UnassignDeviceFromVehicleRequestDTO request
    ) {

        // 1. Resolve the authenticated user id from the jwt.
        UUID unassignedBy = extractUserId(authentication);

        // 2. Maps transport data to an application command.
        UnassignDeviceFromVehicleCommand command = VehicleAssignmentHttpMapper.toUnassignDeviceFromVehicleCommand(
                request, vehicleId, unassignedBy
        );

        // 3. Delegates assignment closure to the application layer.
        VehicleAssignmentResult result = vehicleAssignmentUseCase.unassignDeviceFromVehicle(command);

        // 4. Converts application output into the response payload.
        VehicleAssignmentDTO responseData = VehicleAssignmentHttpMapper.toDTO(result);

        return ResponseEntity.ok(new ApiResponseDTO<>("Device unassigned from vehicle successfully.", responseData));
    }

    /**
     * Retrieves the assignment history for a vehicle.
     *
     * @param vehicleId vehicle identifier.
     * @param page page number using one-based indexing.
     * @param size amount of items requested per page.
     * @return paged vehicle assignment history wrapped in an API response.
     */
    @Operation(
            summary = "Retrieves the assignment history for a vehicle.",
            description = """
                    Accepts a vehicle identifier and pagination parameters, retrieves the paginated assignment history
                    for the specified vehicle, and returns the paginated assignment information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the vehicle assignment history"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The pagination parameters are invalid"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The vehicle with the specified ID was not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the assignment history request"
                    )
            }
    )
    @GetMapping(VEHICLES_ASSIGNMENTS_BASE_URI)
    public ResponseEntity<ApiResponseDTO<PageDTO<VehicleAssignmentDTO>>> getVehicleAssignmentHistory(
            JwtAuthenticationToken authentication,
            @PathVariable UUID vehicleId,
            @RequestParam(name = PAGE_NUMBER_PARAM, defaultValue = "1") int page,
            @RequestParam(name = PAGE_SIZE_PARAM, defaultValue = "10") int size
    ) {

        // 1. Resolve the authenticated user id from the jwt.
        UUID userId = extractUserId(authentication);

        // 2. Maps transport data to an application command.
        GetVehicleAssignmentHistoryCommand command = VehicleAssignmentHttpMapper.toGetVehicleAssignmentHistoryCommand(
                vehicleId, userId, page, size
        );

        // 3. Delegates retrieval of the assignment history to the application layer.
        PageResult<VehicleAssignmentResult> result = vehicleAssignmentUseCase.getVehicleAssignmentHistory(command);

        // 4. Converts application output into the response payload.
        PageDTO<VehicleAssignmentDTO> responseData = VehicleAssignmentHttpMapper.toPageDTO(result);

        return ResponseEntity.ok(new ApiResponseDTO<>("Vehicle assignment history fetched successfully.", responseData));
    }

    private UUID extractUserId(JwtAuthenticationToken authentication) {

        // 1. Keycloak stores the user id in the token subject claim.
        String subject = authentication.getToken().getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Missing user ID in authentication token.");
        }

        // 2. Converts subject to UUID used by application/domain contracts.
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid user ID format in authentication token.", exception);
        }
    }

}
