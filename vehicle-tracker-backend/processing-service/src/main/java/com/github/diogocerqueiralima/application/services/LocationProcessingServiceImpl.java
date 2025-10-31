package com.github.diogocerqueiralima.application.services;

import com.github.diogocerqueiralima.domain.ports.inbound.LocationProcessingService;
import com.github.diogocerqueiralima.location.ReceiveLocationEvent;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LocationProcessingServiceImpl implements LocationProcessingService {

    /**
     *
     * {@inheritDoc}
     * <br/>
     * This method get the device ID from the received location event
     * and call an external service to retrieve the associated vehicle.
     *
     */
    @Override
    public void process(ReceiveLocationEvent event) {

        UUID deviceId = event.deviceId();

    }

}
