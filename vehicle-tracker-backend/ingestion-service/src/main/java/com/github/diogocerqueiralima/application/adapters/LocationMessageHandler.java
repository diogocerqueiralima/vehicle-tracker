package com.github.diogocerqueiralima.application.adapters;

import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import static schemas.v1.location.LocationOuterClass.*;

@Component
public class LocationMessageHandler {

    /**
     *
     * Handler for processing incoming location messages.
     *
     * @return a MessageHandler that processes the incoming messages
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handleLocationMessage() {
        return message -> {

            try {
                Location location = Location.parseFrom((byte[]) message.getPayload());
                System.out.println(location);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }

        };
    }

}
