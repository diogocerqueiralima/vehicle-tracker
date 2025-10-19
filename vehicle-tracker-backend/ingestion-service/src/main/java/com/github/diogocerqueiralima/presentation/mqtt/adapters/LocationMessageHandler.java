package com.github.diogocerqueiralima.application.adapters;

import com.github.diogocerqueiralima.application.commands.ReceiveLocationCommand;
import com.github.diogocerqueiralima.domain.ports.inbound.LocationService;
import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static schemas.v1.location.LocationOuterClass.*;

@Component
public class LocationMessageHandler {

    private final LocationService locationService;

    public LocationMessageHandler(LocationService locationService) {
        this.locationService = locationService;
    }

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
                ReceiveLocationCommand command = new ReceiveLocationCommand(
                        location.getTime(),
                        location.getDate(),
                        location.getLatitude(),
                        location.getLatitudeHemisphere(),
                        location.getLongitude(),
                        location.getLongitudeHemisphere(),
                        location.getAltitude(),
                        location.getSpeed(),
                        location.getHeading(),
                        UUID.randomUUID()
                );

                locationService.receive(command);

            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }

        };
    }

}
