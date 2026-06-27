package com.github.diogocerqueiralima.asset.service.presentation.grpc.mappers;

import com.github.diogocerqueiralima.asset.service.application.results.DeviceResult;
import com.github.diogocerqueiralima.schema.proto.DeviceResponse;
import com.google.protobuf.util.Timestamps;

/**
 * This class is responsible for mapping between gRPC messages and domain models related to devices.
 */
public class DeviceGrpcMapper {

    // Should not be instantiated
    private DeviceGrpcMapper() {}

    public static DeviceResponse toResponse(DeviceResult result) {
        return DeviceResponse.newBuilder()
                .setId(result.id().toString())
                .setOwnerId(result.ownerId().toString())
                .setCreatedAt(Timestamps.fromMillis(result.createdAt().toEpochMilli()))
                .setUpdatedAt(Timestamps.fromMillis(result.updatedAt().toEpochMilli()))
                .setSerialNumber(result.serialNumber())
                .setImei(result.imei())
                .setManufacturer(result.manufacturer())
                .setModel(result.model())
                .build();
    }

}
