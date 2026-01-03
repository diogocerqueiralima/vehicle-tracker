package com.github.diogocerqueiralima.presentation.http.controllers;

import com.github.diogocerqueiralima.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.application.commands.LookupDeviceByIdCommand;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.domain.ports.inbound.DeviceUseCase;
import com.github.diogocerqueiralima.presentation.context.ExecutionContext;
import com.github.diogocerqueiralima.presentation.context.UserExecutionContext;
import com.github.diogocerqueiralima.presentation.http.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.http.dto.CreateDeviceDTO;
import com.github.diogocerqueiralima.presentation.http.dto.DeviceDTO;
import com.github.diogocerqueiralima.presentation.http.mappers.DeviceMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.github.diogocerqueiralima.presentation.http.Paths.DEVICE_PATH;

@RestController
@RequestMapping(DEVICE_PATH)
public class DeviceController {

    private final DeviceMapper deviceMapper;
    private final DeviceUseCase deviceUseCase;

    public DeviceController(@Qualifier("dm-presentation") DeviceMapper deviceMapper, DeviceUseCase deviceUseCase) {
        this.deviceMapper = deviceMapper;
        this.deviceUseCase = deviceUseCase;
    }

    /**
     *
     * Creates a new device.
     *
     * @param dto the DTO containing device creation details
     * @param jwt the JWT token of the authenticated user
     * @return a ResponseEntity containing the created device DTO and a success message
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<DeviceDTO>> create(
            @RequestBody CreateDeviceDTO dto, @AuthenticationPrincipal Jwt jwt
    ) {

        ExecutionContext executionContext = UserExecutionContext.fromJwt(jwt);
        CreateDeviceCommand command = new CreateDeviceCommand(
                dto.imei(), dto.serialNumber(), dto.manufacturer(), dto.vehicleId()
        );
        DeviceResult result = deviceUseCase.create(command, executionContext);
        DeviceDTO resultDTO = deviceMapper.toDTO(result);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(resultDTO, "Device created successfully"));
    }

    /**
     *
     * Retrieves a device by its ID.
     *
     * @param id the UUID of the device to retrieve
     * @param jwt the JWT token of the authenticated user
     * @return a ResponseEntity containing the device DTO and a success message
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<DeviceDTO>> getById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {

        ExecutionContext executionContext = UserExecutionContext.fromJwt(jwt);
        LookupDeviceByIdCommand command = new LookupDeviceByIdCommand(id);
        DeviceResult result = deviceUseCase.getById(command, executionContext);
        DeviceDTO resultDTO = deviceMapper.toDTO(result);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponseDTO<>(resultDTO, "Device retrieved successfully"));
    }

    /**
     *
     * Deletes a device by its ID.
     *
     * @param id the UUID of the device to delete
     * @param jwt the JWT token of the authenticated user
     * @return a ResponseEntity containing a success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {

        ExecutionContext executionContext = UserExecutionContext.fromJwt(jwt);
        LookupDeviceByIdCommand command = new LookupDeviceByIdCommand(id);

        deviceUseCase.delete(command, executionContext);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponseDTO<>(null, "Device deleted successfully"));
    }

}
