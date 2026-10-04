package com.github.diogocerqueiralima.asset.service.presentation.http.controllers

import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.SimCardAssignmentUseCase
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.AssignDeviceToSimCardRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.SimCardAssignmentDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UnassignDeviceFromSimCardRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.mappers.SimCardAssignmentHttpMapper
import com.github.diogocerqueiralima.api.common.dto.ApiResponseDTO
import com.github.diogocerqueiralima.api.common.dto.PageDTO
import com.github.diogocerqueiralima.api.common.headers.ReservedHeaders
import com.github.diogocerqueiralima.api.common.uris.ApplicationURIs.*
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

@Tag(name = "SIM Card Assignments", description = "Operations related to SIM card assignments, including assigning and unassigning devices to SIM cards.")
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
class SimCardAssignmentController(private val simCardAssignmentUseCase: SimCardAssignmentUseCase) {

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
            value = [
                    ApiResponse(
                            responseCode = "201",
                            description = "Successfully assigned the device to the SIM card",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Device assigned to SIM card successfully.\", \"data\": {\"device_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"sim_card_id\": \"7c9e6679-7425-40de-944b-e07fc1f90ae7\", \"assigned_at\": \"2024-03-10T14:00:00Z\", \"assigned_by\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\", \"unassigned_at\": null, \"unassigned_by\": null, \"removal_reason\": null, \"active\": true}}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The assignment failed or the request payload is invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "404",
                            description = "The SIM card or device with the specified ID was not found",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the assignment request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @PostMapping(SIM_CARDS_ASSIGNMENTS_BASE_URI)
    fun assignDeviceToSimCard(
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String,
            @PathVariable
            @Parameter(description = "Unique identifier of the SIM card to assign a device to.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            simCardId: UUID,
            @RequestBody request: AssignDeviceToSimCardRequestDTO
    ): ResponseEntity<ApiResponseDTO<SimCardAssignmentDTO>> {

        // 1. Resolve the authenticated user id from the jwt.
        val assignedBy = extractUserId(userIdHeader)

        // 2. Maps transport data to an application command.
        val command = SimCardAssignmentHttpMapper.toAssignDeviceToSimCardCommand(
                request,
                simCardId,
                assignedBy
        )

        // 3. Delegates assignment creation to the application layer.
        val result = simCardAssignmentUseCase.assignDeviceToSimCard(command)

        // 4. Converts application output into the response payload.
        val responseData = SimCardAssignmentHttpMapper.toDTO(result)

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO("Device assigned to SIM card successfully.", responseData))
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
            value = [
                    ApiResponse(
                            responseCode = "200",
                            description = "Successfully unassigned the device from the SIM card",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Device unassigned from SIM card successfully.\", \"data\": {\"device_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"sim_card_id\": \"7c9e6679-7425-40de-944b-e07fc1f90ae7\", \"assigned_at\": \"2024-03-10T14:00:00Z\", \"assigned_by\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\", \"unassigned_at\": null, \"unassigned_by\": null, \"removal_reason\": null, \"active\": true}}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The unassignment failed or the request payload is invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "404",
                            description = "The SIM card or active assignment with the specified ID was not found",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the unassignment request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @DeleteMapping(SIM_CARDS_ASSIGNMENTS_BASE_URI)
    fun unassignDeviceFromSimCard(
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String,
            @PathVariable
            @Parameter(description = "Unique identifier of the SIM card to unassign a device from.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            simCardId: UUID,
            @RequestBody request: UnassignDeviceFromSimCardRequestDTO
    ): ResponseEntity<ApiResponseDTO<SimCardAssignmentDTO>> {

        // 1. Resolve the authenticated user id from the jwt.
        val unassignedBy = extractUserId(userIdHeader)

        // 2. Maps transport data to an application command.
        val command = SimCardAssignmentHttpMapper.toUnassignDeviceFromSimCardCommand(
                request,
                simCardId,
                unassignedBy
        )

        // 3. Delegates assignment closure to the application layer.
        val result = simCardAssignmentUseCase.unassignDeviceFromSimCard(command)

        // 4. Converts application output into the response payload.
        val responseData = SimCardAssignmentHttpMapper.toDTO(result)

        return ResponseEntity.ok(ApiResponseDTO("Device unassigned from SIM card successfully.", responseData))
    }


    private fun extractUserId(userIdHeader: String): UUID {

        if (userIdHeader.isBlank()) {
            throw IllegalArgumentException("Missing user ID header.")
        }

        try {
            return UUID.fromString(userIdHeader)
        } catch (exception: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid user ID format in header.", exception)
        }
    }

}
