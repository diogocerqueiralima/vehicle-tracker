package com.github.diogocerqueiralima.infrastructure.publishers;

import com.github.diogocerqueiralima.domain.model.Location;
import com.github.diogocerqueiralima.domain.ports.outbound.LocationPublisher;
import com.github.diogocerqueiralima.infrastructure.config.ApplicationConfig;
import com.github.diogocerqueiralima.location.ReceiveLocationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class LocationPublisherImpl implements LocationPublisher {

    private final static Logger LOGGER = LoggerFactory.getLogger(LocationPublisherImpl.class);

    private final ApplicationConfig applicationConfig;
    private final RabbitTemplate rabbitTemplate;

    public LocationPublisherImpl(ApplicationConfig applicationConfig, RabbitTemplate rabbitTemplate) {
        this.applicationConfig = applicationConfig;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(Location location) {

        LOGGER.info("Publishing location from device with id: {}", location.deviceId());

        ReceiveLocationEvent event = ReceiveLocationEvent.newBuilder()
                .timestamp(location.timestamp())
                .latitude(location.latitude())
                .longitude(location.longitude())
                .altitude(location.altitude())
                .speed(location.speed())
                .course(location.course())
                .deviceId(location.deviceId())
                .build();

        rabbitTemplate.convertAndSend("", applicationConfig.getLocationQueueName(), event);
    }

}
