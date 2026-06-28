package com.github.diogocerqueiralima.asset.service.presentation.http.controllers;

import com.github.diogocerqueiralima.api.common.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.api.common.dto.PageDTO;
import com.github.diogocerqueiralima.asset.service.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.UpdateDeviceCommand;
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.DeviceUseCase;
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateDeviceRequestDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.DeviceDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.UpdateDeviceRequestDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.mappers.DeviceHttpMapper;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.github.diogocerqueiralima.asset.service.presentation.http.config.ApplicationURIs.*;

/**
 * REST endpoints for device operations.
 */
@Tag(name = "Devices", description = "Operations related to devices, including creation, update, and retrieval.")
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
public class DeviceController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final DeviceUseCase deviceUseCase;

    public DeviceController(DeviceUseCase deviceUseCase) {
        this.deviceUseCase = deviceUseCase;
    }

    /**
     * Creates a new device.
     *
     * @param request request payload for device creation.
     * @return created device wrapped in an API response.
     */
    @Operation(
            summary = "Creates a new device.",
            description = """
                    Accepts a request payload containing device details, creates a new device in the system,
                    and returns the created device information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Successfully created a new device",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Device created successfully.\", \"data\": {\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"created_at\": \"2024-01-15T10:30:00Z\", \"updated_at\": \"2024-06-01T08:00:00Z\", \"serial_number\": \"SN-00123456\", \"model\": \"TrackPro X200\", \"manufacturer\": \"Teltonika\", \"imei\": \"352099001761481\"}}"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The device already exists or the request payload is invalid",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the device creation request",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    )
            }
    )
    @PostMapping(DEVICES_BASE_URI)
    public ResponseEntity<ApiResponseDTO<DeviceDTO>> create(@RequestBody CreateDeviceRequestDTO request) {

        // 1. Maps transport data to an application command.
        CreateDeviceCommand command = DeviceHttpMapper.toCreateCommand(request);

        // 2. Delegates creation to the application layer.
        DeviceResult result = deviceUseCase.create(command);

        // 3. Maps the application result to the response DTO.
        DeviceDTO deviceDTO = DeviceHttpMapper.toDTO(result);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>("Device created successfully.", deviceDTO));
    }

    /**
     * Updates an existing device.
     *
     * @param id device identifier.
     * @param request request payload for device update.
     * @return updated device wrapped in an API response.
     */
    @Operation(
            summary = "Updates an existing device.",
            description = """
                    Accepts a request payload containing updated device details, updates the existing device in the system,
                    and returns the updated device information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated the device",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Device updated successfully.\", \"data\": {\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"created_at\": \"2024-01-15T10:30:00Z\", \"updated_at\": \"2024-06-01T08:00:00Z\", \"serial_number\": \"SN-00123456\", \"model\": \"TrackPro X200\", \"manufacturer\": \"Teltonika\", \"imei\": \"352099001761481\"}}"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The request payload is invalid",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The device with the specified ID was not found",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the device update request",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    )
            }
    )
    @PutMapping(DEVICES_ID_URI)
    public ResponseEntity<ApiResponseDTO<DeviceDTO>> update(
            @Parameter(description = "Unique identifier of the device to update.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable(name = DEVICE_ID_PARAM) UUID id,
            @RequestBody UpdateDeviceRequestDTO request
    ) {

        // 1. Maps transport data to an application command.
        UpdateDeviceCommand command = DeviceHttpMapper.toUpdateCommand(id, request);

        // 2. Delegates update to the application layer.
        DeviceResult result = deviceUseCase.update(command);

        // 3. Maps the application result to the response DTO.
        DeviceDTO deviceDTO = DeviceHttpMapper.toDTO(result);

        return ResponseEntity.ok(
                new ApiResponseDTO<>("Device updated successfully.", deviceDTO)
        );
    }

    /**
     * Retrieves a device by id.
     *
     * @param id device identifier.
     * @return device wrapped in an API response.
     */
    @Operation(
            summary = "Retrieves a device by id.",
            description = """
                    Accepts a device identifier, retrieves the corresponding device from the system,
                    and returns the device information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the device",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Device fetched successfully.\", \"data\": {\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"created_at\": \"2024-01-15T10:30:00Z\", \"updated_at\": \"2024-06-01T08:00:00Z\", \"serial_number\": \"SN-00123456\", \"model\": \"TrackPro X200\", \"manufacturer\": \"Teltonika\", \"imei\": \"352099001761481\"}}"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The device identifier is invalid",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The device with the specified ID was not found",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the device retrieval request",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    )
            }
    )
    @GetMapping(DEVICES_ID_URI)
    public ResponseEntity<ApiResponseDTO<DeviceDTO>> getById(
            @Parameter(description = "Unique identifier of the device to retrieve.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable(name = DEVICE_ID_PARAM) UUID id,
            JwtAuthenticationToken authentication
    ) {

        // 1. Resolves the authenticated user id from Keycloak token subject.
        UUID userId = extractUserId(authentication);

        // 2. Detects admin access from mapped Spring Security authorities.
        boolean isAdmin = hasAdminRole(authentication);

        // 3. Maps transport data to an application command.
        GetDeviceByIdCommand command = DeviceHttpMapper.toGetByIdCommand(id, userId, isAdmin);

        // 4. Delegates retrieval to the application layer.
        DeviceResult result = deviceUseCase.getById(command);

        // 5. Maps the application result to the response DTO.
        DeviceDTO deviceDTO = DeviceHttpMapper.toDTO(result);

        return ResponseEntity.ok(
                new ApiResponseDTO<>("Device fetched successfully.", deviceDTO)
        );
    }

    /**
     * Retrieves a one-based pageNumber of devices.
     *
     * @param pageNumber pageNumber number using one-based indexing.
     * @param pageSize amount of items requested per pageNumber.
     * @return paged devices wrapped in an API response.
     */
    @Operation(
            summary = "Retrieves a one-based pageNumber of devices.",
            description = """
                    Accepts pagination parameters, retrieves a paginated list of devices from the system,
                    and returns the paginated device information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the paginated list of devices",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Devices fetched successfully.\", \"data\": {\"page_number\": 1, \"page_size\": 10, \"total_pages\": 1, \"total_elements\": 1, \"data\": [{\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"created_at\": \"2024-01-15T10:30:00Z\", \"updated_at\": \"2024-06-01T08:00:00Z\", \"serial_number\": \"SN-00123456\", \"model\": \"TrackPro X200\", \"manufacturer\": \"Teltonika\", \"imei\": \"352099001761481\"}]}}"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The pagination parameters are invalid",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the device pagination request",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    )
            }
    )
    @GetMapping(DEVICES_BASE_URI)
    public ResponseEntity<ApiResponseDTO<PageDTO<DeviceDTO>>> getPage(
            JwtAuthenticationToken authentication,
            @Parameter(description = "Page number using one-based indexing.", example = "1")
            @RequestParam(name = PAGE_NUMBER_PARAM, defaultValue = "1") int pageNumber,
            @Parameter(description = "Number of devices per page.", example = "10")
            @RequestParam(name = PAGE_SIZE_PARAM, defaultValue = "10") int pageSize
    ) {

        // 1. Resolves the authenticated user id from Keycloak token subject.
        UUID userId = extractUserId(authentication);

        // 2. Maps query params to application command.
        GetDevicePageCommand command = DeviceHttpMapper.toGetPageCommand(pageNumber, pageSize, userId);

        // 3. Delegates retrieval of the pageNumber to the application layer.
        PageResult<DeviceResult> result = deviceUseCase.getPage(command);

        // 4. Converts application result to transport DTO.
        PageDTO<DeviceDTO> pageDTO = DeviceHttpMapper.toPageDTO(result);

        return ResponseEntity.ok(
                new ApiResponseDTO<>("Devices fetched successfully.", pageDTO)
        );
    }

    private UUID extractUserId(JwtAuthenticationToken authentication) {

        // 1. Keycloak stores the user id in token subject claim.
        String subject = authentication.getToken().getSubject();

        // 2. Converts subject to UUID used by application/domain contracts.
        return UUID.fromString(subject);
    }

    private boolean hasAdminRole(JwtAuthenticationToken authentication) {

        // 1. Uses granted authorities already mapped by SecurityConfig converter.
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equalsIgnoreCase(authority.getAuthority()));
    }

}

