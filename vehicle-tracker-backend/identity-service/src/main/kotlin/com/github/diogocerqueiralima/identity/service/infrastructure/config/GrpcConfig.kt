package com.github.diogocerqueiralima.identity.service.infrastructure.config

import com.github.diogocerqueiralima.schema.proto.DeviceServiceGrpc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.grpc.client.GrpcChannelFactory

/**
 * This class is responsible for configuring gRPC-related settings and beans in the application context.
 */
@Configuration
class GrpcConfig {

    /**
     * Creates a gRPC blocking stub for the DeviceService, allowing synchronous communication with the asset-service.
     *
     * @param grpcChannelFactory the factory used to create gRPC channels for communication
     * @return a blocking stub for the DeviceService, enabling synchronous gRPC calls to the asset-service
     */
    @Bean
    fun blockingStub(grpcChannelFactory: GrpcChannelFactory): DeviceServiceGrpc.DeviceServiceBlockingStub =
        DeviceServiceGrpc.newBlockingStub(grpcChannelFactory.createChannel("asset-service"))

}
