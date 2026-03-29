package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.AssignDeviceToVehicleCommand;
import com.github.diogocerqueiralima.application.commands.UnassignDeviceFromVehicleCommand;
import com.github.diogocerqueiralima.application.ports.inbound.VehicleAssignmentUseCase;
import com.github.diogocerqueiralima.application.results.VehicleAssignmentResult;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.AssignDeviceToVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.UnassignDeviceFromVehicleRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.VehicleAssignmentDTO;
import com.github.diogocerqueiralima.presentation.mappers.VehicleAssignmentPresentationMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.*;

/**
 * REST endpoints for vehicle assignment operations.
 */
@RestController
public class VehicleAssignmentController {

    private final VehicleAssignmentUseCase vehicleAssignmentUseCase;

    public VehicleAssignmentController(VehicleAssignmentUseCase vehicleAssignmentUseCase) {
        this.vehicleAssignmentUseCase = vehicleAssignmentUseCase;
    }

    /**
     * Assigns a device to a vehicle.
     *
     * @param request request payload for assignment.
     * @return created assignment wrapped in an API response.
     */
    @PostMapping(VEHICLES_ASSIGNMENTS_ASSIGN_URI)
    public ResponseEntity<ApiResponseDTO<VehicleAssignmentDTO>> assignDeviceToVehicle(
            @Valid @RequestBody AssignDeviceToVehicleRequestDTO request
    ) {

        // TODO: get the user from authentication context
        UUID assignedBy = UUID.randomUUID();

        // 1. Maps transport data to an application command.
        AssignDeviceToVehicleCommand command = VehicleAssignmentPresentationMapper.toAssignDeviceToVehicleCommand(
                request, assignedBy
        );

        // 2. Delegates assignment creation to the application layer.
        VehicleAssignmentResult result = vehicleAssignmentUseCase.assignDeviceToVehicle(command);

        // 3. Converts application output into the response payload.
        VehicleAssignmentDTO responseData = VehicleAssignmentPresentationMapper.toDTO(result);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>("Device assigned to vehicle successfully.", responseData));
    }

    /**
     * Unassigns a device from a vehicle.
     *
     * @param request request payload for unassignment.
     * @return updated assignment wrapped in an API response.
     */
    @PostMapping(VEHICLES_ASSIGNMENTS_UNASSIGN_URI)
    public ResponseEntity<ApiResponseDTO<VehicleAssignmentDTO>> unassignDeviceFromVehicle(
            @Valid @RequestBody UnassignDeviceFromVehicleRequestDTO request
    ) {

        // TODO: get the user from authentication context
        UUID unassignedBy = UUID.randomUUID();

        // 1. Maps transport data to an application command.
        UnassignDeviceFromVehicleCommand command = VehicleAssignmentPresentationMapper.toUnassignDeviceFromVehicleCommand(
                request,
                unassignedBy
        );

        // 2. Delegates assignment closure to the application layer.
        VehicleAssignmentResult result = vehicleAssignmentUseCase.unassignDeviceFromVehicle(command);

        // 3. Converts application output into the response payload.
        VehicleAssignmentDTO responseData = VehicleAssignmentPresentationMapper.toDTO(result);

        return ResponseEntity.ok(new ApiResponseDTO<>("Device unassigned from vehicle successfully.", responseData));
    }

}

