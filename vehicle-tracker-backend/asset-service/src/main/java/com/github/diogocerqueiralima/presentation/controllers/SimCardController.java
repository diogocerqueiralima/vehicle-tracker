package com.github.diogocerqueiralima.presentation.controllers;

import com.github.diogocerqueiralima.application.commands.CreateSimCardCommand;
import com.github.diogocerqueiralima.application.commands.DeleteSimCardByIccidCommand;
import com.github.diogocerqueiralima.application.commands.GetSimCardByIccidCommand;
import com.github.diogocerqueiralima.application.commands.UpdateSimCardCommand;
import com.github.diogocerqueiralima.application.ports.inbound.SimCardUseCase;
import com.github.diogocerqueiralima.application.results.SimCardResult;
import com.github.diogocerqueiralima.presentation.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.presentation.dto.CreateSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.dto.SimCardDTO;
import com.github.diogocerqueiralima.presentation.dto.UpdateSimCardRequestDTO;
import com.github.diogocerqueiralima.presentation.mappers.SimCardPresentationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.SIM_CARDS_BASE_URI;
import static com.github.diogocerqueiralima.presentation.config.ApplicationURIs.SIM_CARDS_ICCID_URI;

/**
 * REST endpoints for SIM card operations.
 */
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
    @PostMapping(SIM_CARDS_BASE_URI)
    public ResponseEntity<ApiResponseDTO<SimCardDTO>> create(@RequestBody CreateSimCardRequestDTO request) {

        // 1. Maps transport data to an application command.
        CreateSimCardCommand command = SimCardPresentationMapper.toCreateCommand(request);

        // 2. Delegates creation to the application layer.
        SimCardResult result = simCardUseCase.create(command);

        // 3. Maps the application result to the response DTO.
        SimCardDTO simCardDTO = SimCardPresentationMapper.toDTO(result);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>("SIM card created successfully.", simCardDTO));
    }

    /**
     * Updates an existing SIM card.
     *
     * @param iccid SIM card ICCID.
     * @param request request payload for SIM card update.
     * @return updated SIM card wrapped in an API response.
     */
    @PutMapping(SIM_CARDS_ICCID_URI)
    public ResponseEntity<ApiResponseDTO<SimCardDTO>> update(
            @PathVariable String iccid,
            @RequestBody UpdateSimCardRequestDTO request
    ) {

        // 1. Maps transport data to an application command.
        UpdateSimCardCommand command = SimCardPresentationMapper.toUpdateCommand(iccid, request);

        // 2. Delegates update to the application layer.
        SimCardResult result = simCardUseCase.update(command);

        // 3. Maps the application result to the response DTO.
        SimCardDTO simCardDTO = SimCardPresentationMapper.toDTO(result);

        return ResponseEntity.ok(new ApiResponseDTO<>("SIM card updated successfully.", simCardDTO));
    }

    /**
     * Retrieves a SIM card by ICCID.
     *
     * @param iccid SIM card ICCID.
     * @return SIM card wrapped in an API response.
     */
    @GetMapping(SIM_CARDS_ICCID_URI)
    public ResponseEntity<ApiResponseDTO<SimCardDTO>> getByIccid(@PathVariable String iccid) {

        // 1. Maps transport data to an application command.
        GetSimCardByIccidCommand command = SimCardPresentationMapper.toGetByIccidCommand(iccid);

        // 2. Delegates retrieval to the application layer.
        SimCardResult result = simCardUseCase.getByIccid(command);

        // 3. Maps the application result to the response DTO.
        SimCardDTO simCardDTO = SimCardPresentationMapper.toDTO(result);

        return ResponseEntity.ok(new ApiResponseDTO<>("SIM card fetched successfully.", simCardDTO));
    }

    /**
     * Deletes a SIM card by ICCID.
     *
     * @param iccid SIM card ICCID.
     * @return success response with no payload.
     */
    @DeleteMapping(SIM_CARDS_ICCID_URI)
    public ResponseEntity<ApiResponseDTO<Void>> deleteByIccid(@PathVariable String iccid) {

        // 1. Maps transport data to an application command.
        DeleteSimCardByIccidCommand command = SimCardPresentationMapper.toDeleteByIccidCommand(iccid);

        // 2. Delegates deletion to the application layer.
        simCardUseCase.deleteByIccid(command);

        return ResponseEntity.ok(new ApiResponseDTO<>("SIM card deleted successfully.", null));
    }

}

