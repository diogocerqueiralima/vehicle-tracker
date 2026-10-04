package com.github.diogocerqueiralima.asset.service.presentation.http.controllers

import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.VehicleAssignmentUseCase
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.AssignDeviceToVehicleRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UnassignDeviceFromVehicleRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.VehicleAssignmentDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.mappers.VehicleAssignmentHttpMapper
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

@Tag(name = "Vehicle Assignments", description = "Operations related to vehicle assignments, including assigning and unassigning devices to vehicles.")
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
class VehicleAssignmentController(private val vehicleAssignmentUseCase: VehicleAssignmentUseCase) {

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
            value = [
                    ApiResponse(
                            responseCode = "201",
                            description = "Successfully assigned the device to the vehicle",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Device assigned to vehicle successfully.\", \"data\": {\"device_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"vehicle_id\": \"7c9e6679-7425-40de-944b-e07fc1f90ae7\", \"assigned_at\": \"2024-03-10T14:00:00Z\", \"assigned_by\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\", \"unassigned_at\": null, \"unassigned_by\": null, \"removal_reason\": null, \"installed_by\": \"b2c3d4e5-f6a7-8901-bcde-f12345678901\", \"notes\": \"Installed on front dashboard.\", \"active\": true}}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The assignment failed or the request payload is invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "404",
                            description = "The vehicle or device with the specified ID was not found",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the assignment request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @PostMapping(VEHICLES_ASSIGNMENTS_BASE_URI)
    fun assignDeviceToVehicle(
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String,
            @Parameter(description = "Unique identifier of the vehicle to assign a device to.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable
            vehicleId: UUID,
            @RequestBody request: AssignDeviceToVehicleRequestDTO
    ): ResponseEntity<ApiResponseDTO<VehicleAssignmentDTO>> {

        // 1. Resolve the authenticated user id from the jwt.
        val assignedBy = extractUserId(userIdHeader)

        // 2. Maps transport data to an application command.
        val command = VehicleAssignmentHttpMapper.toAssignDeviceToVehicleCommand(
                request, vehicleId, assignedBy
        )

        // 3. Delegates assignment creation to the application layer.
        val result = vehicleAssignmentUseCase.assignDeviceToVehicle(command)

        // 4. Converts application output into the response payload.
        val responseData = VehicleAssignmentHttpMapper.toDTO(result)

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO("Device assigned to vehicle successfully.", responseData))
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
            value = [
                    ApiResponse(
                            responseCode = "200",
                            description = "Successfully unassigned the device from the vehicle",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Device unassigned from vehicle successfully.\", \"data\": {\"device_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"vehicle_id\": \"7c9e6679-7425-40de-944b-e07fc1f90ae7\", \"assigned_at\": \"2024-03-10T14:00:00Z\", \"assigned_by\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\", \"unassigned_at\": null, \"unassigned_by\": null, \"removal_reason\": null, \"installed_by\": \"b2c3d4e5-f6a7-8901-bcde-f12345678901\", \"notes\": \"Installed on front dashboard.\", \"active\": true}}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The unassignment failed or the request payload is invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "404",
                            description = "The vehicle or active assignment with the specified ID was not found",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the unassignment request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @DeleteMapping(VEHICLES_ASSIGNMENTS_BASE_URI)
    fun unassignDeviceFromVehicle(
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String,
            @Parameter(description = "Unique identifier of the vehicle to unassign a device from.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable
            vehicleId: UUID,
            @RequestBody request: UnassignDeviceFromVehicleRequestDTO
    ): ResponseEntity<ApiResponseDTO<VehicleAssignmentDTO>> {

        // 1. Resolve the authenticated user id from the jwt.
        val unassignedBy = extractUserId(userIdHeader)

        // 2. Maps transport data to an application command.
        val command = VehicleAssignmentHttpMapper.toUnassignDeviceFromVehicleCommand(
                request, vehicleId, unassignedBy
        )

        // 3. Delegates assignment closure to the application layer.
        val result = vehicleAssignmentUseCase.unassignDeviceFromVehicle(command)

        // 4. Converts application output into the response payload.
        val responseData = VehicleAssignmentHttpMapper.toDTO(result)

        return ResponseEntity.ok(ApiResponseDTO("Device unassigned from vehicle successfully.", responseData))
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
            value = [
                    ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the vehicle assignment history",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Vehicle assignment history fetched successfully.\", \"data\": {\"page_number\": 1, \"page_size\": 10, \"total_pages\": 1, \"total_elements\": 1, \"data\": [{\"device_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"vehicle_id\": \"7c9e6679-7425-40de-944b-e07fc1f90ae7\", \"assigned_at\": \"2024-03-10T14:00:00Z\", \"assigned_by\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\", \"unassigned_at\": null, \"unassigned_by\": null, \"removal_reason\": null, \"installed_by\": \"b2c3d4e5-f6a7-8901-bcde-f12345678901\", \"notes\": \"Installed on front dashboard.\", \"active\": true}]}}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The pagination parameters are invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "404",
                            description = "The vehicle with the specified ID was not found",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the assignment history request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @GetMapping(VEHICLES_ASSIGNMENTS_BASE_URI)
    fun getVehicleAssignmentHistory(
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String,
            @Parameter(description = "Unique identifier of the vehicle whose assignment history to retrieve.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable
            vehicleId: UUID,
            @Parameter(description = "Page number using one-based indexing.", example = "1")
            @RequestParam(name = PAGE_NUMBER_PARAM, defaultValue = "1")
            page: Int,
            @Parameter(description = "Number of assignment records per page.", example = "10")
            @RequestParam(name = PAGE_SIZE_PARAM, defaultValue = "10")
            size: Int
    ): ResponseEntity<ApiResponseDTO<PageDTO<VehicleAssignmentDTO>>> {

        // 1. Resolve the authenticated user id from the jwt.
        val userId = extractUserId(userIdHeader)

        // 2. Maps transport data to an application command.
        val command = VehicleAssignmentHttpMapper.toGetVehicleAssignmentHistoryCommand(
                vehicleId, userId, page, size
        )

        // 3. Delegates retrieval of the assignment history to the application layer.
        val result = vehicleAssignmentUseCase.getVehicleAssignmentHistory(command)

        // 4. Converts application output into the response payload.
        val responseData = VehicleAssignmentHttpMapper.toPageDTO(result)

        return ResponseEntity.ok(ApiResponseDTO("Vehicle assignment history fetched successfully.", responseData))
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
