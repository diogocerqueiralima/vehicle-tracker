package com.github.diogocerqueiralima.presentation.amqp.listeners;

import com.github.diogocerqueiralima.application.commands.LocationCommand;
import com.github.diogocerqueiralima.domain.ports.inbound.LocationProcessingUseCase;
import com.github.diogocerqueiralima.location.ReceiveLocationEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReceiveLocationListener {

    private static final Logger log = LoggerFactory.getLogger(ReceiveLocationListener.class);
    private final LocationProcessingUseCase locationProcessingUseCase;

    public ReceiveLocationListener(LocationProcessingUseCase locationProcessingUseCase) {
        this.locationProcessingUseCase = locationProcessingUseCase;
    }

    /**
     *
     * Handler for processing incoming location events from the RabbitMQ queue.
     * Converts the received event into a LocationCommand and delegates processing to the LocationProcessingUseCase.
     *
     * @param event the received location event
     */
    @RabbitListener(queues = "#{applicationConfig.locationQueueName}")
    public void onReceiveLocation(ReceiveLocationEvent event) {

        log.info("Received location event for device id: {}", event.deviceId());

        locationProcessingUseCase.process(
                new LocationCommand(
                        event.timestamp(),
                        event.latitude(),
                        event.longitude(),
                        event.altitude(),
                        event.speed(),
                        event.course(),
                        event.deviceId()
                )
        );

    }

}