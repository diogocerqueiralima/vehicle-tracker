package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.assets.Device;
import com.github.diogocerqueiralima.infrastructure.entities.assets.DeviceEntity;

public class DeviceMapper {

    // Should not be instantiated
    private DeviceMapper() {}

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
