package com.github.diogocerqueiralima.infrastructure.publishers;

import com.github.diogocerqueiralima.domain.model.Location;
import com.github.diogocerqueiralima.domain.ports.outbound.LocationPublisher;
import org.springframework.stereotype.Component;

@Component
public class LocationPublisherImpl implements LocationPublisher {

    @Override
    public void publish(Location location) {
        System.out.println(location);
    }
}
