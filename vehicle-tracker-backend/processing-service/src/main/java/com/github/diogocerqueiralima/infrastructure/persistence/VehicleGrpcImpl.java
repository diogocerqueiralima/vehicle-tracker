package com.github.diogocerqueiralima.infrastructure.persistence;

import com.github.diogocerqueiralima.domain.model.Vehicle;
import com.github.diogocerqueiralima.domain.ports.outbound.VehicleGrpc;
import com.github.diogocerqueiralima.proto.DeviceId;
import com.github.diogocerqueiralima.proto.VehicleResponse;
import com.github.diogocerqueiralima.proto.VehicleServiceGrpc;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class VehicleGrpcImpl implements VehicleGrpc {

    private final VehicleServiceGrpc.VehicleServiceBlockingStub vehicleServiceStub;

    public VehicleGrpcImpl(VehicleServiceGrpc.VehicleServiceBlockingStub vehicleServiceStub) {
        this.vehicleServiceStub = vehicleServiceStub;
    }

    @Override
    public Optional<Vehicle> findByDeviceId(UUID id) {

        try {

            DeviceId deviceId = DeviceId.newBuilder().setId(id.toString()).build();
            VehicleResponse response = vehicleServiceStub.getByDeviceId(deviceId);
            Vehicle vehicle = new Vehicle(UUID.fromString(response.getId()));

            return Optional.of(vehicle);

        } catch (StatusRuntimeException e) {
            return Optional.empty();
        }

    }

}
