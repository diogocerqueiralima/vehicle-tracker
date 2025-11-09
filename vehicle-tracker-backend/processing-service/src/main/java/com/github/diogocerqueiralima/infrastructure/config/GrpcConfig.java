package com.github.diogocerqueiralima.infrastructure.config;

import com.github.diogocerqueiralima.proto.VehicleServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcConfig {

    @Bean
    public VehicleServiceGrpc.VehicleServiceBlockingStub vehicleServiceStub(GrpcChannelFactory channels) {
        return VehicleServiceGrpc.newBlockingStub(channels.createChannel("vehicle-service"));
    }

}