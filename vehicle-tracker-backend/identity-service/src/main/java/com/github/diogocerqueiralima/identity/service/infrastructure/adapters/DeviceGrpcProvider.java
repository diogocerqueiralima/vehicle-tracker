package com.github.diogocerqueiralima.identity.service.infrastructure.adapters;

import com.github.diogocerqueiralima.identity.service.domain.model.device.Device;
import com.github.diogocerqueiralima.identity.service.domain.ports.outbound.DeviceProvider;
import com.github.diogocerqueiralima.identity.service.infrastructure.mappers.DeviceMapper;
import com.github.diogocerqueiralima.schema.proto.DeviceId;
import com.github.diogocerqueiralima.schema.proto.DeviceIsOwnedByUserRequest;
import com.github.diogocerqueiralima.schema.proto.DeviceResponse;
import com.github.diogocerqueiralima.schema.proto.DeviceServiceGrpc;
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

    @Override
    public boolean isOwnedBy(UUID deviceId, UUID userId) {

        try {

            // 1. The remote service answers by completing the call, and reports a device the user
            // does not own by failing it with PERMISSION_DENIED. The is_owned field it sends back
            // is not set, so the status is what carries the answer.
            return blockingStub
                    .deviceIsOwnedByUser(
                            DeviceIsOwnedByUserRequest.newBuilder()
                                    .setDeviceId(deviceId.toString())
                                    .setUserId(userId.toString())
                                    .build()
                    )
                    .getIsOwned();

        } catch (StatusRuntimeException e) {

            // 2. A refusal is an answer, not a failure: the device is not this user's.
            if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
                return false;
            }

            // 3. Anything else leaves ownership unknown, which must not be read as permission.
            throw e;
        }

    }

}
