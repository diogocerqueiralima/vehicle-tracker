package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.Location;

/**
 * Port interface for publishing location data to external services.
 */
public interface LocationPublisher {

    /**
     *
     * Publishes the given location to an external service.
     * The other components system should consume this data.
     *
     * @param location the location data to be published
     */
    void publish(Location location);

}
