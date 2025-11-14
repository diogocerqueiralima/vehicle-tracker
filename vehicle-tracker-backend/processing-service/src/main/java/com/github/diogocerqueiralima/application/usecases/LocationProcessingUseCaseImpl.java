package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.LocationCommand;
import com.github.diogocerqueiralima.application.exceptions.VehicleNotFoundException;
import com.github.diogocerqueiralima.domain.model.LocationSnapshot;
import com.github.diogocerqueiralima.domain.model.Vehicle;
import com.github.diogocerqueiralima.domain.ports.inbound.LocationProcessingUseCase;
import com.github.diogocerqueiralima.domain.ports.outbound.LocationSnapshotPersistence;
import com.github.diogocerqueiralima.domain.ports.outbound.VehicleGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LocationProcessingUseCaseImpl implements LocationProcessingUseCase {

    private static final Logger log = LoggerFactory.getLogger(LocationProcessingUseCaseImpl.class);

    private final VehicleGrpc vehicleGrpc;
    private final LocationSnapshotPersistence locationSnapshotPersistence;

    public LocationProcessingUseCaseImpl(
            VehicleGrpc vehicleGrpc, LocationSnapshotPersistence locationSnapshotPersistence
    ) {
        this.vehicleGrpc = vehicleGrpc;
        this.locationSnapshotPersistence = locationSnapshotPersistence;
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
        Vehicle vehicle = vehicleGrpc.findByDeviceId(deviceId)
                .orElseThrow(() -> new VehicleNotFoundException(deviceId));

        LocationSnapshot snapshot = new LocationSnapshot(
                vehicle,
                command.timestamp(),
                command.latitude(),
                command.longitude(),
                command.altitude(),
                command.speed(),
                command.course()
        );

        locationSnapshotPersistence.save(snapshot);
        log.info("Received location event from device with id: {}", deviceId);
    }

}
