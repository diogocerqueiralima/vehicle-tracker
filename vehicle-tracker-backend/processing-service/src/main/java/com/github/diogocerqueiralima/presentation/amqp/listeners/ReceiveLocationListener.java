package com.github.diogocerqueiralima.presentation.amqp.listeners;

import com.github.diogocerqueiralima.application.commands.LocationCommand;
import com.github.diogocerqueiralima.domain.ports.inbound.LocationProcessingService;
import com.github.diogocerqueiralima.location.ReceiveLocationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReceiveLocationListener {

    private final LocationProcessingService locationProcessingService;

    public ReceiveLocationListener(LocationProcessingService locationProcessingService) {
        this.locationProcessingService = locationProcessingService;
    }

    @RabbitListener(queues = "#{applicationConfig.locationQueueName}")
    public void onReceiveLocation(ReceiveLocationEvent event) {
        locationProcessingService.process(
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