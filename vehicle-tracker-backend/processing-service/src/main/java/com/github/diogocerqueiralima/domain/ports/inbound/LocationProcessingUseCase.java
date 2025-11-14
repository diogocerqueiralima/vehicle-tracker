package com.github.diogocerqueiralima.domain.ports.inbound;

import com.github.diogocerqueiralima.application.commands.LocationCommand;

/**
 * Port for processing location events.
 */
public interface LocationProcessingUseCase {

    /**
     *
     * Process a location command.
     *
     * @param command the location command to process
     */
    void process(LocationCommand command);

}
