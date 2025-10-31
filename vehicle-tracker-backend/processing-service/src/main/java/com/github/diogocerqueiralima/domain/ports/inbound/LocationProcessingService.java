package com.github.diogocerqueiralima.domain.ports.inbound;

import com.github.diogocerqueiralima.location.ReceiveLocationEvent;

/**
 * Port for processing location events.
 */
public interface LocationProcessingService {

    /**
     *
     * Processes a received location event.
     *
     * @param event the location event to be processed
     */
    void process(ReceiveLocationEvent event);

}
