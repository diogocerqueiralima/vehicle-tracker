package com.github.diogocerqueiralima.presentation.http.controllers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromSimCardCommand;
import com.github.diogocerqueiralima.domain.ports.inbound.SimCardAssignmentUseCase;
import com.github.diogocerqueiralima.application.results.SimCardAssignmentResult;
import com.github.diogocerqueiralima.presentation.http.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.http.dto.AssignDeviceToSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.http.dto.SimCardAssignmentDTO;
import com.github.diogocerqueiralima.presentation.http.dto.UnassignDeviceFromSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.http.mappers.SimCardAssignmentHttpMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

import static com.github.diogocerqueiralima.presentation.http.config.ApplicationURIs.SIM_CARD_ID_PARAM;
import static com.github.diogocerqueiralima.presentation.http.config.ApplicationURIs.SIM_CARDS_ASSIGNMENTS_BASE_URI;

/**
 * REST endpoints for SIM card assignment operations.
 */
@Tag(name = "SIM Card Assignments", description = "Operations related to SIM card assignments, including assigning and unassigning devices to SIM cards.")
@SecurityRequirements(value = { @SecurityRequirement(name = "bearerAuth") })
@ApiResponses(
        value = {
                @ApiResponse(
                        responseCode = "401",
                        description = "Missing or invalid JWT bearer token",
                        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "The authenticated user does not have permission to perform this operation",
                        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                )
        }
)
@RestController
public class SimCardAssignmentController {

    private final SimCardAssignmentUseCase simCardAssignmentUseCase;

    public SimCardAssignmentController(SimCardAssignmentUseCase simCardAssignmentUseCase) {
        this.simCardAssignmentUseCase = simCardAssignmentUseCase;
    }

    /**
     * Assigns a device to a SIM card.
     *
     * @param request request payload for assignment.
     * @return created assignment wrapped in an API response.
     */
    @Operation(
            summary = "Assigns a device to a SIM card.",
            description = """
                    Accepts a request payload containing the device identifier, assigns the device to the specified SIM card,
                    and returns the created assignment information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Successfully assigned the device to the SIM card",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Device assigned to SIM card successfully.\", \"data\": {\"device_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"sim_card_id\": \"7c9e6679-7425-40de-944b-e07fc1f90ae7\", \"assigned_at\": \"2024-03-10T14:00:00Z\", \"assigned_by\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\", \"unassigned_at\": null, \"unassigned_by\": null, \"removal_reason\": null, \"active\": true}}"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The assignment failed or the request payload is invalid",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The SIM card or device with the specified ID was not found",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the assignment request",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    )
            }
    )
    @PostMapping(SIM_CARDS_ASSIGNMENTS_BASE_URI)
    public ResponseEntity<ApiResponseDTO<SimCardAssignmentDTO>> assignDeviceToSimCard(
            JwtAuthenticationToken authentication,
            @PathVariable @Parameter(description = "Unique identifier of the SIM card to assign a device to.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true) UUID simCardId,
            @RequestBody AssignDeviceToSimCardRequestDTO request
    ) {

        // 1. Resolve the authenticated user id from the jwt.
        UUID assignedBy = extractUserId(authentication);

        // 2. Maps transport data to an application command.
        AssignDeviceToSimCardCommand command = SimCardAssignmentHttpMapper.toAssignDeviceToSimCardCommand(
                request,
                simCardId,
                assignedBy
        );

        // 3. Delegates assignment creation to the application layer.
        SimCardAssignmentResult result = simCardAssignmentUseCase.assignDeviceToSimCard(command);

        // 4. Converts application output into the response payload.
        SimCardAssignmentDTO responseData = SimCardAssignmentHttpMapper.toDTO(result);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>("Device assigned to SIM card successfully.", responseData));
    }

    /**
     * Unassigns a device from a SIM card.
     *
     * @param request request payload for unassignment.
     * @return updated assignment wrapped in an API response.
     */
    @Operation(
            summary = "Unassigns a device from a SIM card.",
            description = """
                    Accepts a request payload containing the device identifier, closes the active assignment between the device
                    and the specified SIM card, and returns the updated assignment information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully unassigned the device from the SIM card",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Device unassigned from SIM card successfully.\", \"data\": {\"device_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"sim_card_id\": \"7c9e6679-7425-40de-944b-e07fc1f90ae7\", \"assigned_at\": \"2024-03-10T14:00:00Z\", \"assigned_by\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\", \"unassigned_at\": null, \"unassigned_by\": null, \"removal_reason\": null, \"active\": true}}"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The unassignment failed or the request payload is invalid",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The SIM card or active assignment with the specified ID was not found",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the unassignment request",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    )
            }
    )
    @DeleteMapping(SIM_CARDS_ASSIGNMENTS_BASE_URI)
    public ResponseEntity<ApiResponseDTO<SimCardAssignmentDTO>> unassignDeviceFromSimCard(
            JwtAuthenticationToken authentication,
            @PathVariable @Parameter(description = "Unique identifier of the SIM card to unassign a device from.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true) UUID simCardId,
            @RequestBody UnassignDeviceFromSimCardRequestDTO request
    ) {

        // 1. Resolve the authenticated user id from the jwt.
        UUID unassignedBy = extractUserId(authentication);

        // 2. Maps transport data to an application command.
        UnassignDeviceFromSimCardCommand command = SimCardAssignmentHttpMapper.toUnassignDeviceFromSimCardCommand(
                request,
                simCardId,
                unassignedBy
        );

        // 3. Delegates assignment closure to the application layer.
        SimCardAssignmentResult result = simCardAssignmentUseCase.unassignDeviceFromSimCard(command);

        // 4. Converts application output into the response payload.
        SimCardAssignmentDTO responseData = SimCardAssignmentHttpMapper.toDTO(result);

        return ResponseEntity.ok(new ApiResponseDTO<>("Device unassigned from SIM card successfully.", responseData));
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

