package com.github.diogocerqueiralima.infrastructure.adapters;

import com.github.diogocerqueiralima.domain.model.device.Device;
import com.github.diogocerqueiralima.domain.ports.outbound.DeviceProvider;
import com.github.diogocerqueiralima.infrastructure.mappers.DeviceMapper;
import com.github.diogocerqueiralima.proto.DeviceId;
import com.github.diogocerqueiralima.proto.DeviceResponse;
import com.github.diogocerqueiralima.proto.DeviceServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the DeviceProvider interface that uses gRPC to communicate with a remote device service.
 * This class provides methods to retrieve device information from the remote service.
 * It uses a gRPC blocking stub to make synchronous calls to the device service.
 */
@Component
public class DeviceGrpcProvider implements DeviceProvider {

    private final DeviceServiceGrpc.DeviceServiceBlockingStub blockingStub;

    /**
     *
     * Constructs a new DeviceGrpcProvider with the specified gRPC blocking stub.
     *
     * @param blockingStub the gRPC blocking stub used to communicate with the remote device service
     */
    public DeviceGrpcProvider(DeviceServiceGrpc.DeviceServiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    @Override
    public Optional<Device> findById(UUID id) {

        try {

            DeviceResponse response = blockingStub.getDeviceById(
                    DeviceId.newBuilder()
                            .setId(id.toString())
                            .build()
            );

            return Optional.of(DeviceMapper.toDomain(response));

        } catch (StatusRuntimeException e) {

            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                return Optional.empty();
            }

            throw e;
        }

    }

}
