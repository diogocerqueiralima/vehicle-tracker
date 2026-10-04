package com.github.diogocerqueiralima.asset.service.presentation.http.controllers

import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.SimCardUseCase
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateSimCardRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.SimCardDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UpdateSimCardRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.mappers.SimCardHttpMapper
import com.github.diogocerqueiralima.api.common.dto.ApiResponseDTO
import com.github.diogocerqueiralima.api.common.dto.PageDTO
import com.github.diogocerqueiralima.api.common.headers.ReservedHeaders
import com.github.diogocerqueiralima.api.common.uris.ApplicationURIs.SIM_CARDS_BASE_URI
import com.github.diogocerqueiralima.api.common.uris.ApplicationURIs.SIM_CARDS_ID_URI
import com.github.diogocerqueiralima.api.common.uris.ApplicationURIs.SIM_CARD_ID_PARAM
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "SIM Cards", description = "Operations related to SIM cards, including creation, update, retrieval, and deletion.")
@SecurityRequirements(value = [SecurityRequirement(name = "bearerAuth")])
@ApiResponses(
        value = [
                ApiResponse(
                        responseCode = "401",
                        description = "Missing or invalid JWT bearer token",
                        content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                ),
                ApiResponse(
                        responseCode = "403",
                        description = "The authenticated user does not have permission to perform this operation",
                        content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                )
        ]
)
@RestController
class SimCardController(private val simCardUseCase: SimCardUseCase) {

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
            value = [
                    ApiResponse(
                            responseCode = "201",
                            description = "Successfully created a new sim card",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"SIM card created successfully.\", \"data\": {\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"created_at\": \"2024-01-15T10:30:00Z\", \"updated_at\": \"2024-06-01T08:00:00Z\", \"iccid\": \"8955100000000000001\", \"msisdn\": \"+351912345678\", \"imsi\": \"268010000000001\"}}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The sim card already exists or the request payload is invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the sim card creation request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @PostMapping(SIM_CARDS_BASE_URI)
    fun create(
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String,
            @RequestBody request: CreateSimCardRequestDTO
    ): ResponseEntity<ApiResponseDTO<SimCardDTO>> {

        // 1. Resolve the authenticated user id from the jwt
        val userId = extractUserId(userIdHeader)

        // 2. Maps transport data to an application command.
        val command = SimCardHttpMapper.toCreateCommand(request, userId)

        // 3. Delegates creation to the application layer.
        val result = simCardUseCase.create(command)

        // 4. Maps the application result to the response DTO.
        val simCardDTO = SimCardHttpMapper.toDTO(result)

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO("SIM card created successfully.", simCardDTO))
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
            value = [
                    ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated the sim card",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"SIM card updated successfully.\", \"data\": {\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"created_at\": \"2024-01-15T10:30:00Z\", \"updated_at\": \"2024-06-01T08:00:00Z\", \"iccid\": \"8955100000000000001\", \"msisdn\": \"+351912345678\", \"imsi\": \"268010000000001\"}}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The request payload is invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "404",
                            description = "The sim card with the specified ID was not found",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the sim card update request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @PutMapping(SIM_CARDS_ID_URI)
    fun update(
            @Parameter(description = "Unique identifier of the SIM card to update.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable(name = SIM_CARD_ID_PARAM)
            id: UUID,
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String,
            @RequestBody request: UpdateSimCardRequestDTO
    ): ResponseEntity<ApiResponseDTO<SimCardDTO>> {

        // 1. Resolve the authenticated user id from the jwt
        val userId = extractUserId(userIdHeader)

        // 2. Maps transport data to an application command.
        val command = SimCardHttpMapper.toUpdateCommand(id, request, userId)

        // 3. Delegates update to the application layer.
        val result = simCardUseCase.update(command)

        // 4. Maps the application result to the response DTO.
        val simCardDTO = SimCardHttpMapper.toDTO(result)

        return ResponseEntity.ok(ApiResponseDTO("SIM card updated successfully.", simCardDTO))
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
            value = [
                    ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the sim card",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"SIM card fetched successfully.\", \"data\": {\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"created_at\": \"2024-01-15T10:30:00Z\", \"updated_at\": \"2024-06-01T08:00:00Z\", \"iccid\": \"8955100000000000001\", \"msisdn\": \"+351912345678\", \"imsi\": \"268010000000001\"}}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The sim card identifier is invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "404",
                            description = "The sim card with the specified ID was not found",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the sim card retrieval request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @GetMapping(SIM_CARDS_ID_URI)
    fun getById(
            @Parameter(description = "Unique identifier of the SIM card to retrieve.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable(name = SIM_CARD_ID_PARAM)
            id: UUID,
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String
    ): ResponseEntity<ApiResponseDTO<SimCardDTO>> {

        // 1. Resolve the authenticated user id from the jwt
        val userId = extractUserId(userIdHeader)

        // 2. Maps transport data to an application command.
        val command = SimCardHttpMapper.toGetByIdCommand(id, userId)

        // 3. Delegates retrieval to the application layer.
        val result = simCardUseCase.getById(command)

        // 4. Maps the application result to the response DTO.
        val simCardDTO = SimCardHttpMapper.toDTO(result)

        return ResponseEntity.ok(ApiResponseDTO("SIM card fetched successfully.", simCardDTO))
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
            value = [
                    ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted the sim card",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"SIM card deleted successfully.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The sim card identifier is invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "404",
                            description = "The sim card with the specified ID was not found",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the sim card deletion request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @DeleteMapping(SIM_CARDS_ID_URI)
    fun deleteById(
            @Parameter(description = "Unique identifier of the SIM card to delete.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable(SIM_CARD_ID_PARAM)
            id: UUID,
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String
    ): ResponseEntity<ApiResponseDTO<Void?>> {

        // 1. Resolve the authenticated user id from the jwt
        val userId = extractUserId(userIdHeader)

        // 2. Maps transport data to an application command.
        val command = SimCardHttpMapper.toDeleteByIdCommand(id, userId)

        // 3. Delegates deletion to the application layer.
        simCardUseCase.deleteById(command)

        return ResponseEntity.ok(ApiResponseDTO("SIM card deleted successfully.", null))
    }


    private fun extractUserId(userIdHeader: String): UUID = UUID.fromString(userIdHeader)

}
