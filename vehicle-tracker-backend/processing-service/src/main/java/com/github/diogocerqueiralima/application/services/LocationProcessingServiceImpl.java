package com.github.diogocerqueiralima.application.services;

import com.github.diogocerqueiralima.application.commands.LocationCommand;
import com.github.diogocerqueiralima.domain.ports.inbound.LocationProcessingService;
import com.github.diogocerqueiralima.domain.ports.outbound.VehicleGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LocationProcessingServiceImpl implements LocationProcessingService {

    private static final Logger log = LoggerFactory.getLogger(LocationProcessingServiceImpl.class);

    private final VehicleGrpc vehicleGrpc;

    public LocationProcessingServiceImpl(VehicleGrpc vehicleGrpc) {
        this.vehicleGrpc = vehicleGrpc;
    }

    /**
     *
     * {@inheritDoc}
     * <br/>
     * This method get the device ID from the received location event
     * and call an external service to retrieve the associated vehicle.
     *
     */
    @Override
    public void process(LocationCommand command) {

        UUID deviceId = command.deviceId();
        vehicleGrpc.findById(deviceId).ifPresent(System.out::println);

        log.info("Received location event from device with id: {}", deviceId);
    }

}
