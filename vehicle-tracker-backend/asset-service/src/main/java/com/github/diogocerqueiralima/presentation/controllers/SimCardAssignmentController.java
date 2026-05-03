package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToSimCardCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromSimCardCommand;
import com.github.diogocerqueiralima.application.ports.inbound.SimCardAssignmentUseCase;
import com.github.diogocerqueiralima.application.results.SimCardAssignmentResult;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.AssignDeviceToSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.SimCardAssignmentDTO;
import com.github.diogocerqueiralima.presentation.dto.UnassignDeviceFromSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.mappers.SimCardAssignmentPresentationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.SIM_CARDS_ASSIGNMENTS_BASE_URI;

/**
 * REST endpoints for SIM card assignment operations.
 */
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
    @PostMapping(SIM_CARDS_ASSIGNMENTS_BASE_URI)
    public ResponseEntity<ApiResponseDTO<SimCardAssignmentDTO>> assignDeviceToSimCard(
            JwtAuthenticationToken authentication,
            @RequestBody AssignDeviceToSimCardRequestDTO request
    ) {

        // 1. Resolve the authenticated user id from the jwt.
        UUID assignedBy = extractUserId(authentication);

        // 2. Maps transport data to an application command.
        AssignDeviceToSimCardCommand command = SimCardAssignmentPresentationMapper.toAssignDeviceToSimCardCommand(
                request,
                assignedBy
        );

        // 3. Delegates assignment creation to the application layer.
        SimCardAssignmentResult result = simCardAssignmentUseCase.assignDeviceToSimCard(command);

        // 4. Converts application output into the response payload.
        SimCardAssignmentDTO responseData = SimCardAssignmentPresentationMapper.toDTO(result);

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
    @DeleteMapping(SIM_CARDS_ASSIGNMENTS_BASE_URI)
    public ResponseEntity<ApiResponseDTO<SimCardAssignmentDTO>> unassignDeviceFromSimCard(
            JwtAuthenticationToken authentication,
            @RequestBody UnassignDeviceFromSimCardRequestDTO request
    ) {

        // 1. Resolve the authenticated user id from the jwt.
        UUID unassignedBy = extractUserId(authentication);

        // 2. Maps transport data to an application command.
        UnassignDeviceFromSimCardCommand command = SimCardAssignmentPresentationMapper.toUnassignDeviceFromSimCardCommand(
                request,
                unassignedBy
        );

        // 3. Delegates assignment closure to the application layer.
        SimCardAssignmentResult result = simCardAssignmentUseCase.unassignDeviceFromSimCard(command);

        // 4. Converts application output into the response payload.
        SimCardAssignmentDTO responseData = SimCardAssignmentPresentationMapper.toDTO(result);

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

