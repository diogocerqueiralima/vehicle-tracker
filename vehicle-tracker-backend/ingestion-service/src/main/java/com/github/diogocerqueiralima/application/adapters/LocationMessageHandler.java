package com.github.diogocerqueiralima.application.adapters;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

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
            System.out.println(message.getPayload());
        };
    }

}
