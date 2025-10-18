package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.Location;

public interface LocationPublisher {

    /**
     *
     * Publishes the given location to an external service.
     *
     * @param location the location data to be published
     */
    void publish(Location location);

}
