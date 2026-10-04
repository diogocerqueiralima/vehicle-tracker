package com.github.diogocerqueiralima.ingestion.service.infrastructure.publishers

import com.github.diogocerqueiralima.asset.service.location.ReceiveLocationEvent
import com.github.diogocerqueiralima.asset.service.location.config.ApplicationConfig
import com.github.diogocerqueiralima.ingestion.service.domain.model.Location
import com.github.diogocerqueiralima.ingestion.service.domain.ports.outbound.LocationPublisher
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component

private val LOGGER = LoggerFactory.getLogger(LocationPublisherImpl::class.java)

@Import(ApplicationConfig::class)
@Component
class LocationPublisherImpl(
    private val applicationConfig: ApplicationConfig,
    private val rabbitTemplate: RabbitTemplate
) : LocationPublisher {

    override fun publish(location: Location) {

        LOGGER.info("Publishing location from device with id: {}", location.deviceId)

        val event = ReceiveLocationEvent(
            timestamp = location.timestamp,
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            speed = location.speed,
            course = location.course,
            deviceId = location.deviceId
        )

        rabbitTemplate.convertAndSend("", applicationConfig.locationQueueName, event)
    }

}
