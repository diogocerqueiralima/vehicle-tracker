package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.model.LocationSnapshot;
import com.github.diogocerqueiralima.domain.ports.outbound.LocationSnapshotPersistence;
import com.github.diogocerqueiralima.infrastructure.entities.LocationSnapshotEntity;
import com.github.diogocerqueiralima.infrastructure.repositories.LocationSnapshotRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class LocationSnapshotPersistenceImpl implements LocationSnapshotPersistence {

    private final LocationSnapshotRepository locationSnapshotRepository;

    public LocationSnapshotPersistenceImpl(LocationSnapshotRepository locationSnapshotRepository) {
        this.locationSnapshotRepository = locationSnapshotRepository;
    }

    @Override
    public void save(LocationSnapshot snapshot) {

        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
        Point point = gf.createPoint(
                new Coordinate(snapshot.getLongitude(), snapshot.getLatitude(), snapshot.getAltitude())
        );

        LocationSnapshotEntity snapshotEntity = new LocationSnapshotEntity();
        snapshotEntity.setId(snapshot.getId());
        snapshotEntity.setVehicleId(snapshot.getVehicle().getId());
        snapshotEntity.setPoint(point);
        snapshotEntity.setSpeed(snapshot.getSpeed());
        snapshotEntity.setCourse(snapshot.getCourse());

        locationSnapshotRepository.save(snapshotEntity);
    }

}