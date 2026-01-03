package com.github.diogocerqueiralima.domain.ports.inbound;

import com.github.diogocerqueiralima.application.commands.ReceiveLocationCommand;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * Use case interface for handling location data.
 */
@Validated
public interface LocationUseCase {

    /**
     * Receives a location command and forwards it for further handling.
     * This should convert the NMEA formatted data into a Location model
     *
     * @param command the location data to be forwarded
     */
    void receive(@Valid ReceiveLocationCommand command);

}
