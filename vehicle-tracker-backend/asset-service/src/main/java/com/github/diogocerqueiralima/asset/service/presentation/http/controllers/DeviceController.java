package com.github.diogocerqueiralima.asset.service.presentation.http.controllers;

import com.github.diogocerqueiralima.api.common.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.api.common.dto.PageDTO;
import com.github.diogocerqueiralima.api.common.headers.ReservedHeaders;
import com.github.diogocerqueiralima.asset.service.application.commands.CreateOrUpdateDeviceCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.asset.service.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.asset.service.domain.ports.inbound.DeviceUseCase;
import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult;
import com.github.diogocerqueiralima.asset.service.application.results.PageResult;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.CreateOrUpdateDeviceRequestDTO;
import com.github.diogocerqueiralima.asset.service.presentation.http.dto.DeviceDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.UUID;

import static com.github.diogocerqueiralima.api.common.uris.ApplicationURIs.*;

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

    private final DeviceUseCase deviceUseCase;

    public DeviceController(DeviceUseCase deviceUseCase) {
        this.deviceUseCase = deviceUseCase;
    }

    /**
     * Creates a new device or updates an existing one, both identified by the id supplied by the client.
     *
     * @param id device identifier, supplied by the client (e.g. the identity a device generates for itself).
     * @param request request payload for device creation/update.
     * @return saved device wrapped in an API response.
     */
    @Operation(
            summary = "Creates or updates a device.",
            description = """
                    Accepts a device identifier and a request payload containing device details, creates the
                    device if no device with that id exists yet or updates it otherwise, and returns the
                    resulting device information in the response.
                    """
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully created or updated the device",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Device saved successfully.\", \"data\": {\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"created_at\": \"2024-01-15T10:30:00Z\", \"updated_at\": \"2024-06-01T08:00:00Z\", \"serial_number\": \"SN-00123456\", \"model\": \"TrackPro X200\", \"manufacturer\": \"Teltonika\", \"imei\": \"352099001761481\"}}"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The serial number or IMEI is already used by another device, or the request payload is invalid",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "An unexpected error occurred while processing the device create-or-update request",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"Error message.\", \"data\": null}"))
                    )
            }
    )
    @PutMapping(DEVICES_ID_URI)
    public ResponseEntity<ApiResponseDTO<DeviceDTO>> createOrUpdate(
            @Parameter(description = "Unique identifier of the device.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true)
            @PathVariable(name = DEVICE_ID_PARAM) UUID id,
            @RequestBody CreateOrUpdateDeviceRequestDTO request
    ) {

        // 1. Maps transport data to an application command.
        CreateOrUpdateDeviceCommand command = DeviceHttpMapper.toCommand(id, request);

        // 2. Delegates creation/update to the application layer.
        DeviceResult result = deviceUseCase.createOrUpdate(command);

        // 3. Maps the application result to the response DTO.
        DeviceDTO deviceDTO = DeviceHttpMapper.toDTO(result);

        return ResponseEntity.ok(
                new ApiResponseDTO<>("Device saved successfully.", deviceDTO)
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
            @RequestHeader(ReservedHeaders.USER_ID) String userIdHeader,
            @RequestHeader(value = ReservedHeaders.USER_ROLES, required = false) String rolesHeader
    ) {

        // 1. Resolves the authenticated user id from the header injected by the api-gateway.
        UUID userId = extractUserId(userIdHeader);

        // 2. Detects admin access from the roles header injected by the api-gateway.
        boolean isAdmin = hasAdminRole(rolesHeader);

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
            @RequestHeader(ReservedHeaders.USER_ID) String userIdHeader,
            @Parameter(description = "Page number using one-based indexing.", example = "1")
            @RequestParam(name = PAGE_NUMBER_PARAM, defaultValue = "1") int pageNumber,
            @Parameter(description = "Number of devices per page.", example = "10")
            @RequestParam(name = PAGE_SIZE_PARAM, defaultValue = "10") int pageSize
    ) {

        // 1. Resolves the authenticated user id from the header injected by the api-gateway.
        UUID userId = extractUserId(userIdHeader);

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

    private UUID extractUserId(String userIdHeader) {
        return UUID.fromString(userIdHeader);
    }

    private boolean hasAdminRole(String rolesHeader) {

        if (rolesHeader == null || rolesHeader.isBlank()) {
            return false;
        }

        // 1. Roles arrive as a comma-separated list, without the "ROLE_" prefix.
        return Arrays.stream(rolesHeader.split(","))
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.trim()));
    }

}

