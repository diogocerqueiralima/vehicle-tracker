package com.github.diogocerqueiralima.asset.service.presentation.http.controllers

import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.VehicleUseCase
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateVehicleRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UpdateVehicleRequestDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.VehicleDTO
import com.github.diogocerqueiralima.asset.service.presentation.http.mappers.VehicleHttpMapper
import com.github.diogocerqueiralima.api.common.dto.ApiResponseDTO
import com.github.diogocerqueiralima.api.common.dto.PageDTO
import com.github.diogocerqueiralima.api.common.headers.ReservedHeaders
import com.github.diogocerqueiralima.api.common.uris.ApplicationURIs.PAGE_NUMBER_PARAM
import com.github.diogocerqueiralima.api.common.uris.ApplicationURIs.PAGE_SIZE_PARAM
import com.github.diogocerqueiralima.api.common.uris.ApplicationURIs.VEHICLES_BASE_URI
import com.github.diogocerqueiralima.api.common.uris.ApplicationURIs.VEHICLES_ID_URI
import com.github.diogocerqueiralima.api.common.uris.ApplicationURIs.VEHICLE_ID_PARAM
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

@Tag(name = "Vehicles", description = "Operations related to vehicles, including creation, update, and retrieval.")
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
class VehicleController(private val vehicleUseCase: VehicleUseCase) {

    /**
     * Creates a new vehicle.
     *
     * @param request request payload for vehicle creation.
     * @return created vehicle wrapped in an API response.
     */
    @Operation(
            summary = "Creates a new vehicle.",
            description = """
                    Accepts a request payload containing vehicle details, creates a new vehicle in the system,
                    and returns the created vehicle information in the response.
                    """
    )
    @ApiResponses(
            value = [
                    ApiResponse(
                            responseCode = "201",
                            description = "Successfully created a new vehicle",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Vehicle created successfully.\", \"data\": {\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"created_at\": \"2024-01-15T10:30:00Z\", \"updated_at\": \"2024-06-01T08:00:00Z\", \"vin\": \"1HGCM82633A123456\", \"plate\": \"AB-12-CD\", \"model\": \"Civic\", \"manufacturer\": \"Honda\", \"manufacturing_date\": \"2020-03-15\"}}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The vehicle already exists or the request payload is invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the vehicle creation request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @PostMapping(VEHICLES_BASE_URI)
    fun create(
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String,
            @RequestBody request: CreateVehicleRequestDTO
    ): ResponseEntity<ApiResponseDTO<VehicleDTO>> {

        // 1. Resolve the authenticated user id from the jwt
        val userId = extractUserId(userIdHeader)

        // 2. Map transport data to an application command.
        val command = VehicleHttpMapper.toCreateCommand(request, userId)

        // 3. Delegate creation to the application layer.
        val result = vehicleUseCase.create(command)

        // 4. Map the application result to the response DTO.
        val vehicleDTO = VehicleHttpMapper.toDTO(result)

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO("Vehicle created successfully.", vehicleDTO))
    }

    /**
     * Updates an existing vehicle.
     *
     * @param id vehicle identifier.
     * @param request request payload for vehicle update.
     * @return updated vehicle wrapped in an API response.
     */
    @Operation(
            summary = "Updates an existing vehicle.",
            description = """
                    Accepts a request payload containing updated vehicle details, updates the existing vehicle in the system,
                    and returns the updated vehicle information in the response.
                    """
    )
    @ApiResponses(
            value = [
                    ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated the vehicle",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Vehicle updated successfully.\", \"data\": {\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"created_at\": \"2024-01-15T10:30:00Z\", \"updated_at\": \"2024-06-01T08:00:00Z\", \"vin\": \"1HGCM82633A123456\", \"plate\": \"AB-12-CD\", \"model\": \"Civic\", \"manufacturer\": \"Honda\", \"manufacturing_date\": \"2020-03-15\"}}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The request payload is invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "404",
                            description = "The vehicle with the specified ID was not found",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the vehicle update request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @PutMapping(VEHICLES_ID_URI)
    fun update(
            @Parameter(description = "Unique identifier of the vehicle to update.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable(name = VEHICLE_ID_PARAM)
            id: UUID,
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String,
            @RequestBody request: UpdateVehicleRequestDTO
    ): ResponseEntity<ApiResponseDTO<VehicleDTO>> {

        // 1. Resolve the authenticated user id from the jwt
        val userId = extractUserId(userIdHeader)

        // 2. Map transport data to an application command.
        val command = VehicleHttpMapper.toUpdateCommand(id, request, userId)

        // 3. Delegate update to the application layer.
        val result = vehicleUseCase.update(command)

        // 4. Map the application result to the response DTO.
        val vehicleDTO = VehicleHttpMapper.toDTO(result)

        return ResponseEntity.ok(
                ApiResponseDTO("Vehicle updated successfully.", vehicleDTO)
        )
    }

    /**
     * Retrieves a vehicle by id.
     *
     * @param id vehicle identifier.
     * @return vehicle wrapped in an API response.
     */
    @Operation(
            summary = "Retrieves a vehicle by id.",
            description = """
                    Accepts a vehicle identifier, retrieves the corresponding vehicle from the system,
                    and returns the vehicle information in the response.
                    """
    )
    @ApiResponses(
            value = [
                    ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the vehicle",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Vehicle fetched successfully.\", \"data\": {\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"created_at\": \"2024-01-15T10:30:00Z\", \"updated_at\": \"2024-06-01T08:00:00Z\", \"vin\": \"1HGCM82633A123456\", \"plate\": \"AB-12-CD\", \"model\": \"Civic\", \"manufacturer\": \"Honda\", \"manufacturing_date\": \"2020-03-15\"}}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The vehicle identifier is invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "404",
                            description = "The vehicle with the specified ID was not found",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the vehicle retrieval request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @GetMapping(VEHICLES_ID_URI)
    fun getById(
            @Parameter(description = "Unique identifier of the vehicle to retrieve.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable(name = VEHICLE_ID_PARAM)
            id: UUID,
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String
    ): ResponseEntity<ApiResponseDTO<VehicleDTO>> {

        // 1. Resolve the authenticated user id from the jwt.
        val userId = extractUserId(userIdHeader)

        // 2. Map transport data to an application command.
        val command = VehicleHttpMapper.toGetByIdCommand(id, userId)

        // 3. Delegate retrieval to the application layer.
        val result = vehicleUseCase.getById(command)

        // 4. Map the application result to the response DTO.
        val vehicleDTO = VehicleHttpMapper.toDTO(result)

        return ResponseEntity.ok(
                ApiResponseDTO("Vehicle fetched successfully.", vehicleDTO)
        )
    }

    /**
     * Retrieves a one-based pageNumber of vehicles.
     *
     * @param pageNumber pageNumber number using one-based indexing.
     * @param pageSize amount of items requested per pageNumber.
     * @return paged vehicles wrapped in an API response.
     */
    @Operation(
            summary = "Retrieves a one-based pageNumber of vehicles.",
            description = """
                    Accepts pagination parameters, retrieves a paginated list of vehicles from the system,
                    and returns the paginated vehicle information in the response.
                    """
    )
    @ApiResponses(
            value = [
                    ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the paginated list of vehicles",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Vehicles fetched successfully.\", \"data\": {\"page_number\": 1, \"page_size\": 10, \"total_pages\": 1, \"total_elements\": 1, \"data\": [{\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"created_at\": \"2024-01-15T10:30:00Z\", \"updated_at\": \"2024-06-01T08:00:00Z\", \"vin\": \"1HGCM82633A123456\", \"plate\": \"AB-12-CD\", \"model\": \"Civic\", \"manufacturer\": \"Honda\", \"manufacturing_date\": \"2020-03-15\"}]}}")])]
                    ),
                    ApiResponse(
                            responseCode = "400",
                            description = "The pagination parameters are invalid",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    ),
                    ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the vehicle pagination request",
                            content = [Content(mediaType = "application/json", examples = [ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}")])]
                    )
            ]
    )
    @GetMapping(VEHICLES_BASE_URI)
    fun getPage(
            @RequestHeader(ReservedHeaders.USER_ID) userIdHeader: String,
            @Parameter(description = "Page number using one-based indexing.", example = "1")
            @RequestParam(name = PAGE_NUMBER_PARAM, defaultValue = "1")
            pageNumber: Int,
            @Parameter(description = "Number of vehicles per page.", example = "10")
            @RequestParam(name = PAGE_SIZE_PARAM, defaultValue = "10")
            pageSize: Int
    ): ResponseEntity<ApiResponseDTO<PageDTO<VehicleDTO>>> {

        // 1. Resolve the authenticated user id from the jwt
        val userId = extractUserId(userIdHeader)

        // 2. Maps query params to application command.
        val command = VehicleHttpMapper.toGetPageCommand(pageNumber, pageSize, userId)

        // 3. Delegates retrieval of the pageNumber to the application layer.
        val result = vehicleUseCase.getPage(command)

        // 4. Converts application result to transport DTO.
        val pageDTO = VehicleHttpMapper.toPageDTO(result)

        return ResponseEntity.ok(
                ApiResponseDTO("Vehicles fetched successfully.", pageDTO)
        )
    }


    private fun extractUserId(userIdHeader: String): UUID = UUID.fromString(userIdHeader)

}
