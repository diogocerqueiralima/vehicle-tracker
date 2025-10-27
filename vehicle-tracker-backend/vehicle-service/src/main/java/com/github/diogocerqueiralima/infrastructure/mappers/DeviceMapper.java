package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.Device;
import com.github.diogocerqueiralima.infrastructure.entities.DeviceEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("dm-infrastructure")
public class DeviceMapper {

    private final VehicleMapper vehicleMapper;

    public DeviceMapper(@Qualifier("vm-infrastructure") VehicleMapper vehicleMapper) {
        this.vehicleMapper = vehicleMapper;
    }

    public DeviceEntity toEntity(Device device) {

        DeviceEntity deviceEntity = new DeviceEntity();

        deviceEntity.setId(device.getId());
        deviceEntity.setImei(device.getImei());
        deviceEntity.setSerialNumber(device.getSerialNumber());
        deviceEntity.setManufacturer(device.getManufacturer());
        deviceEntity.setVehicle(vehicleMapper.toEntity(device.getVehicle()));

        return deviceEntity;
    }

    public Device toDomain(DeviceEntity deviceEntity) {
        return new Device(
                deviceEntity.getId(),
                deviceEntity.getImei(),
                deviceEntity.getSerialNumber(),
                deviceEntity.getManufacturer(),
                vehicleMapper.toDomain(deviceEntity.getVehicle())
        );
    }

}
