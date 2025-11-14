package com.github.diogocerqueiralima.presentation.mqtt.handlers;

import com.github.diogocerqueiralima.application.commands.ReceiveLocationCommand;
import com.github.diogocerqueiralima.domain.ports.inbound.LocationUseCase;
import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import static com.github.diogocerqueiralima.proto.v1.location.LocationOuterClass.Location;

import java.util.UUID;

@Component
public class LocationMessageHandler {

    private final LocationUseCase locationUseCase;

    public LocationMessageHandler(LocationUseCase locationUseCase) {
        this.locationUseCase = locationUseCase;
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

                locationUseCase.receive(command);

            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }

        };
    }

}
