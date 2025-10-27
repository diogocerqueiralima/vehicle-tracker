package com.github.diogocerqueiralima.presentation.http.mappers;

import com.github.diogocerqueiralima.application.results.DeviceResult;
import com.github.diogocerqueiralima.presentation.http.dto.DeviceDTO;
import org.springframework.stereotype.Component;

@Component("dm-presentation")
public class DeviceMapper {

    public DeviceDTO toDTO(DeviceResult deviceResult) {
        return new DeviceDTO(
                deviceResult.id(),
                deviceResult.imei(),
                deviceResult.serialNumber(),
                deviceResult.manufacturer(),
                deviceResult.vehicleId()
        );
    }

}
