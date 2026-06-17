package com.github.diogocerqueiralima.infrastructure.mappers;

import com.github.diogocerqueiralima.domain.model.device.Device;
import com.github.diogocerqueiralima.proto.DeviceResponse;

import java.util.UUID;

/**
 * This class is responsible for mapping between Device domain objects and their corresponding gRPC responses.
 */
public class DeviceMapper {

    // Should not be instantiated
    private DeviceMapper() {}

    /**
     *
     * Converts a {@link DeviceResponse} object to a {@link Device} domain object.
     *
     * @param response the {@link DeviceResponse} object to be converted
     * @return a {@link Device} domain object corresponding to the provided response
     */
    public static Device toDomain(DeviceResponse response) {
        return new Device(
                UUID.fromString(response.getId()),
                UUID.fromString(response.getOwnerId())
        );
    }

}
