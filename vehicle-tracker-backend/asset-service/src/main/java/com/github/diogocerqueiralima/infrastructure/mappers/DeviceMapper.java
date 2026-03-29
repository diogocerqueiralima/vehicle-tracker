package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.assets.Device;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;

/**
 * Mapper for device conversions between domain and persistence layers.
 */
public class DeviceMapper {

    // Should not be instantiated
    private DeviceMapper() {}

    /**
     *
     * Builds a persistence entity from a domain device.
     *
     * @param device domain device with the data to be persisted.
     * @return persistence entity with the provided data and timestamps ready for persistence.
     */
    public static DeviceEntity toEntity(Device device) {

        DeviceEntity entity = new DeviceEntity();

        entity.setId(device.getId());
        entity.setCreatedAt(device.getCreatedAt());
        entity.setUpdatedAt(device.getUpdatedAt());
        entity.setSerialNumber(device.getSerialNumber());
        entity.setModel(device.getModel());
        entity.setManufacturer(device.getManufacturer());
        entity.setImei(device.getImei());

        return entity;
    }

    /**
     *
     * Builds a domain device from a persistence entity.
     *
     * @param entity persistence entity with the device data and timestamps.
     * @return domain device with the provided data and timestamps.
     */
    public static Device toDomain(DeviceEntity entity) {
        return new Device(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getSerialNumber(),
                entity.getModel(),
                entity.getManufacturer(),
                entity.getImei()
        );
    }

}
