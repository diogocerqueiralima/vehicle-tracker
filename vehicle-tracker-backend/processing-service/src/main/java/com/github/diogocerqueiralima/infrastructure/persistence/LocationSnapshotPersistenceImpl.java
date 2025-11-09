package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.model.LocationSnapshot;
import com.github.diogocerqueiralima.domain.ports.outbound.LocationSnapshotPersistence;
import com.github.diogocerqueiralima.infrastructure.entities.LocationSnapshotEntity;
import com.github.diogocerqueiralima.infrastructure.repositories.LocationSnapshotRepository;
import org.springframework.stereotype.Component;

@Component
public class LocationSnapshotPersistenceImpl implements LocationSnapshotPersistence {

    private final LocationSnapshotRepository locationSnapshotRepository;

    public LocationSnapshotPersistenceImpl(LocationSnapshotRepository locationSnapshotRepository) {
        this.locationSnapshotRepository = locationSnapshotRepository;
    }

    @Override
    public void save(LocationSnapshot snapshot) {

        LocationSnapshotEntity snapshotEntity = new LocationSnapshotEntity();
        snapshotEntity.setId(snapshot.getId());
        snapshotEntity.setVehicleId(snapshot.getVehicle().getId());
        snapshotEntity.setLatitude(snapshot.getLatitude());
        snapshotEntity.setLongitude(snapshot.getLongitude());
        snapshotEntity.setAltitude(snapshot.getAltitude());
        snapshotEntity.setSpeed(snapshot.getSpeed());
        snapshotEntity.setCourse(snapshot.getCourse());

        locationSnapshotRepository.save(snapshotEntity);
    }

}