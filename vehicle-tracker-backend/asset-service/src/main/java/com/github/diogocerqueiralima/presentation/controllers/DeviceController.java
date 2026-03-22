package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.CreateDeviceCommand;
import com.github.diogocerqueiralima.application.commands.GetDeviceByIdCommand;
import com.github.diogocerqueiralima.application.commands.GetDevicePageCommand;
import com.github.diogocerqueiralima.application.commands.UpdateDeviceCommand;
import com.github.diogocerqueiralima.application.ports.inbound.DeviceUseCase;
import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.application.results.PageResult;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.CreateDeviceRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.DeviceDTO;
import com.github.diogocerqueiralima.presentation.dto.PageDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateDeviceRequestDTO;
import com.github.diogocerqueiralima.presentation.mappers.DevicePresentationMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.DEVICES_BASE_URI;
import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.DEVICES_ID_URI;
import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.DEVICE_PAGE_NUMBER_PARAM;
import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.DEVICE_PAGE_SIZE_PARAM;

/**
 * REST endpoints for device operations.
 */
@RestController
public class DeviceController {

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
    @PostMapping(DEVICES_BASE_URI)
    public ResponseEntity<ApiResponseDTO<DeviceDTO>> create(@Valid @RequestBody CreateDeviceRequestDTO request) {

        // 1. Maps transport data to an application command.
        CreateDeviceCommand command = DevicePresentationMapper.toCreateCommand(request);

        // 2. Delegates creation to the application layer.
        DeviceResult result = deviceUseCase.create(command);

        // 3. Maps the application result to the response DTO.
        DeviceDTO deviceDTO = DevicePresentationMapper.toDTO(result);

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
    @PutMapping(DEVICES_ID_URI)
    public ResponseEntity<ApiResponseDTO<DeviceDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeviceRequestDTO request
    ) {

        // 1. Maps transport data to an application command.
        UpdateDeviceCommand command = DevicePresentationMapper.toUpdateCommand(id, request);

        // 2. Delegates update to the application layer.
        DeviceResult result = deviceUseCase.update(command);

        // 3. Maps the application result to the response DTO.
        DeviceDTO deviceDTO = DevicePresentationMapper.toDTO(result);

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
    @GetMapping(DEVICES_ID_URI)
    public ResponseEntity<ApiResponseDTO<DeviceDTO>> getById(@PathVariable UUID id) {

        // 1. Maps transport data to an application command.
        GetDeviceByIdCommand command = DevicePresentationMapper.toGetByIdCommand(id);

        // 2. Delegates retrieval to the application layer.
        DeviceResult result = deviceUseCase.getById(command);

        // 3. Maps the application result to the response DTO.
        DeviceDTO deviceDTO = DevicePresentationMapper.toDTO(result);

        return ResponseEntity.ok(
                new ApiResponseDTO<>("Device fetched successfully.", deviceDTO)
        );
    }

    /**
     * Retrieves a one-based page of devices.
     *
     * @param pageNumber page number using one-based indexing.
     * @param pageSize amount of items requested per page.
     * @return paged devices wrapped in an API response.
     */
    @GetMapping(DEVICES_BASE_URI)
    public ResponseEntity<ApiResponseDTO<PageDTO<DeviceDTO>>> getPage(
            @RequestParam(DEVICE_PAGE_NUMBER_PARAM) int pageNumber,
            @RequestParam(DEVICE_PAGE_SIZE_PARAM) int pageSize
    ) {

        // 1. Maps query params to application command.
        GetDevicePageCommand command = DevicePresentationMapper.toGetPageCommand(pageNumber, pageSize);

        // 2. Delegates retrieval of the page to the application layer.
        PageResult<DeviceResult> result = deviceUseCase.getPage(command);

        // 3. Converts application result to transport DTO.
        PageDTO<DeviceDTO> pageDTO = DevicePresentationMapper.toPageDTO(result);

        return ResponseEntity.ok(
                new ApiResponseDTO<>("Devices fetched successfully.", pageDTO)
        );
    }

}

