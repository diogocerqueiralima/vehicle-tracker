package com.github.diogocerqueiralima.presentation.http.controllers;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.DeleteSimCardByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetSimCardByIdCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.domain.ports.inbound.SimCardUseCase;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.presentation.http.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.http.dto.CreateSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.http.dto.SimCardDTO;
import com.github.diogocerqueiralima.presentation.http.dto.UpdateSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.http.mappers.SimCardHttpMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.github.diogocerqueiralima.presentation.http.config.ApplicationURIs.*;

/**
 * REST endpoints for SIM card operations.
 */
@Tag(name = "SIM Cards", description = "Operations related to SIM cards, including creation, update, retrieval, and deletion.")
@SecurityRequirements(value = { @SecurityRequirement(name = "bearerAuth") })
@RestController
public class SimCardController {

    private final SimCardUseCase simCardUseCase;

    public SimCardController(SimCardUseCase simCardUseCase) {
        this.simCardUseCase = simCardUseCase;
    }

    /**
     * Creates a new SIM card.
     *
     * @param request request payload for SIM card creation.
     * @return created SIM card wrapped in an API response.
     */
    @Operation(
            summary = "Creates a new sim card.",
            description = """
                    Accepts a request payload containing sim card details, creates a new sim card in the system,
                    and returns the created sim card information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Successfully created a new sim card"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The sim card already exists or the request payload is invalid"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the sim card creation request"
                    )
            }
    )
    @PostMapping(SIM_CARDS_BASE_URI)
    public ResponseEntity<ApiResponseDTO<SimCardDTO>> create(
            JwtAuthenticationToken authentication,
            @RequestBody CreateSimCardRequestDTO request
    ) {

        // 1. Resolve the authenticated user id from the jwt
        UUID userId = extractUserId(authentication);

        // 2. Maps transport data to an application command.
        CreateSimCardCommand command = SimCardHttpMapper.toCreateCommand(request, userId);

        // 3. Delegates creation to the application layer.
        SimCardResult result = simCardUseCase.create(command);

        // 4. Maps the application result to the response DTO.
        SimCardDTO simCardDTO = SimCardHttpMapper.toDTO(result);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>("SIM card created successfully.", simCardDTO));
    }

    /**
     * Updates an existing SIM card.
     *
     * @param id SIM card id.
     * @param request request payload for SIM card update.
     * @return updated SIM card wrapped in an API response.
     */
    @Operation(
            summary = "Updates an existing sim card.",
            description = """
                    Accepts a request payload containing updated sim card details, updates the existing sim card in the system,
                    and returns the updated sim card information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated a new sim card"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The request payload is invalid"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The sim card with the specified ID was not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the sim card update request"
                    )
            }
    )
    @PutMapping(SIM_CARDS_ID_URI)
    public ResponseEntity<ApiResponseDTO<SimCardDTO>> update(
            @PathVariable(name = SIM_CARD_ID_PARAM) UUID id,
            JwtAuthenticationToken authentication,
            @RequestBody UpdateSimCardRequestDTO request
    ) {

        // 1. Resolve the authenticated user id from the jwt
        UUID userId = extractUserId(authentication);

        // 2. Maps transport data to an application command.
        UpdateSimCardCommand command = SimCardHttpMapper.toUpdateCommand(id, request, userId);

        // 3. Delegates update to the application layer.
        SimCardResult result = simCardUseCase.update(command);

        // 4. Maps the application result to the response DTO.
        SimCardDTO simCardDTO = SimCardHttpMapper.toDTO(result);

        return ResponseEntity.ok(new ApiResponseDTO<>("SIM card updated successfully.", simCardDTO));
    }

    /**
     * Retrieves a SIM card by id.
     *
     * @param id SIM card id.
     * @return SIM card wrapped in an API response.
     */
    @Operation(
            summary = "Retrieves a sim card by id.",
            description = """
                    Accepts a sim card identifier, retrieves the corresponding sim card from the system,
                    and returns the sim card information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the sim card"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The sim card identifier is invalid"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The sim card with the specified ID was not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the sim card retrieval request"
                    )
            }
    )
    @GetMapping(SIM_CARDS_ID_URI)
    public ResponseEntity<ApiResponseDTO<SimCardDTO>> getById(
            @PathVariable(name = SIM_CARD_ID_PARAM) UUID id,
            JwtAuthenticationToken authentication
    ) {

        // 1. Resolve the authenticated user id from the jwt
        UUID userId = extractUserId(authentication);

        // 2. Maps transport data to an application command.
        GetSimCardByIdCommand command = SimCardHttpMapper.toGetByIdCommand(id, userId);

        // 3. Delegates retrieval to the application layer.
        SimCardResult result = simCardUseCase.getById(command);

        // 4. Maps the application result to the response DTO.
        SimCardDTO simCardDTO = SimCardHttpMapper.toDTO(result);

        return ResponseEntity.ok(new ApiResponseDTO<>("SIM card fetched successfully.", simCardDTO));
    }

    /**
     * Deletes a SIM card by id.
     *
     * @param id SIM card id.
     * @return success response with no payload.
     */
    @Operation(
            summary = "Deletes a sim card by id.",
            description = """
                    Accepts a sim card identifier, deletes the corresponding sim card from the system,
                    and returns a success response with no payload.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted the sim card"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The sim card identifier is invalid"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The sim card with the specified ID was not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the sim card deletion request"
                    )
            }
    )
    @DeleteMapping(SIM_CARDS_ID_URI)
    public ResponseEntity<ApiResponseDTO<Void>> deleteById(
            @PathVariable(SIM_CARD_ID_PARAM) UUID id,
            JwtAuthenticationToken authentication
    ) {

        // 1. Resolve the authenticated user id from the jwt
        UUID userId = extractUserId(authentication);

        // 2. Maps transport data to an application command.
        DeleteSimCardByIdCommand command = SimCardHttpMapper.toDeleteByIdCommand(id, userId);

        // 3. Delegates deletion to the application layer.
        simCardUseCase.deleteById(command);

        return ResponseEntity.ok(new ApiResponseDTO<>("SIM card deleted successfully.", null));
    }

    private UUID extractUserId(JwtAuthenticationToken authentication) {
        String subject = authentication.getToken().getSubject();
        return UUID.fromString(subject);
    }

}

