package com.github.diogocerqueiralima.presentation.mqtt.handlers;

import com.github.diogocerqueiralima.application.commands.ReceiveLocationCommand;
import com.github.diogocerqueiralima.domain.ports.inbound.LocationUseCase;
import com.google.protobuf.InvalidProtocolBufferException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static location.LocationOuterClass.Location;

import location.LocationOuterClass;

@Component
public class LocationMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(LocationMessageHandler.class);
    private final LocationUseCase locationUseCase;

    public LocationMessageHandler(LocationUseCase locationUseCase) {
        this.locationUseCase = locationUseCase;
    }

    /**
     *
     * Handler for processing incoming location messages.
     * Converts the received protobuf message into a ReceiveLocationCommand and delegates processing to the LocationUseCase.
     *
     * @return a MessageHandler that processes the incoming messages
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handleLocationMessage() {
        return message -> {

            try {

                log.info("Received location message for device id: {}", UUID.fromString("e29df66b-568e-47af-a866-fc1b6dc321f0"));

                Location location = Location.parseFrom((byte[]) message.getPayload());
                ReceiveLocationCommand command = new ReceiveLocationCommand(
                        location.getTime(),
                        location.getDate(),
                        location.getLatitude(),
                        getHemisphereChar(location.getLatitudeHemisphere()),
                        location.getLongitude(),
                        getHemisphereChar(location.getLongitudeHemisphere()),
                        location.getAltitude(),
                        location.getSpeed(),
                        location.getHeading(),
                        UUID.fromString("e29df66b-568e-47af-a866-fc1b6dc321f0")
                );

                locationUseCase.receive(command);

            } catch (InvalidProtocolBufferException e) {
                log.error("Failed to parse location message", e);
            }

        };
    }

    private String getHemisphereChar(LocationOuterClass.Hemisphere hemisphere ) {
        return hemisphere.toString().charAt(0) + "";
    }

}
