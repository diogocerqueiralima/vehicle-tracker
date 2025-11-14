package com.github.diogocerqueiralima.domain.ports.inbound;

import com.github.diogocerqueiralima.application.commands.ReceiveLocationCommand;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
public interface LocationUseCase {

    /**
     * Receives a location command and forwards it for further handling.
     *
     * @param command the location data to be forwarded
     */
    void receive(@Valid ReceiveLocationCommand command);

}
