package com.github.diogocerqueiralima.application.mappers;

import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.domain.model.Device;
import org.springframework.stereotype.Component;

@Component("dm-application")
public class DeviceMapper {

    public DeviceResult toResult(Device device) {
        return new DeviceResult(
                device.getId(),
                device.getImei(),
                device.getSerialNumber(),
                device.getManufacturer(),
                device.getVehicle().getId()
        );
    }

}
