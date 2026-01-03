package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.Device;
import com.github.diogocerqueiralima.infrastructure.entities.DeviceEntity;
import com.github.diogocerqueiralima.infrastructure.entities.VehicleEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("dm-infrastructure")
public class DeviceMapper {

    private final VehicleMapper vehicleMapper;

    public DeviceMapper(@Qualifier("vm-infrastructure") VehicleMapper vehicleMapper) {
        this.vehicleMapper = vehicleMapper;
    }

    public DeviceEntity toEntity(Device device) {

        VehicleEntity vehicleEntity = vehicleMapper.toEntity(device.getVehicle());
        DeviceEntity deviceEntity = new DeviceEntity();

        vehicleEntity.setDevice(deviceEntity);
        deviceEntity.setId(device.getId());
        deviceEntity.setImei(device.getImei());
        deviceEntity.setSerialNumber(device.getSerialNumber());
        deviceEntity.setManufacturer(device.getManufacturer());
        deviceEntity.setVehicle(vehicleEntity);

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
