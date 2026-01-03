package com.github.diogocerqueiralima.domain.ports.outbound;

import com.github.diogocerqueiralima.domain.model.LocationSnapshot;

/**
 * Port to interact with the location snapshot data source.
 */
public interface LocationSnapshotPersistence {

    /**
     *
     * Saves a location snapshot.
     *
     * @param snapshot the location snapshot to be saved
     */
    void save(LocationSnapshot snapshot);

}
