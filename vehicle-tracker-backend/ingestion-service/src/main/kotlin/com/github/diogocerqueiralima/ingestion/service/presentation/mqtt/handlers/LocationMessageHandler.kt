package com.github.diogocerqueiralima.ingestion.service.presentation.mqtt.handlers

import com.github.diogocerqueiralima.ingestion.service.application.commands.ReceiveLocationCommand
import com.github.diogocerqueiralima.ingestion.service.domain.ports.inbound.LocationUseCase
import com.github.diogocerqueiralima.schema.proto.Hemisphere
import com.github.diogocerqueiralima.schema.proto.Location
import com.google.protobuf.InvalidProtocolBufferException
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.MessageHandler
import org.springframework.stereotype.Component
import java.util.UUID

private val log = LoggerFactory.getLogger(LocationMessageHandler::class.java)

@Component
class LocationMessageHandler(private val locationUseCase: LocationUseCase) {

    /**
     * Handler for processing incoming location messages.
     * Converts the received protobuf message into a [ReceiveLocationCommand] and delegates processing to the [LocationUseCase].
     *
     * @return a [MessageHandler] that processes the incoming messages
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    fun handleLocationMessage(): MessageHandler = MessageHandler { message ->

        try {

            val deviceId = UUID.fromString("e29df66b-568e-47af-a866-fc1b6dc321f0")

            log.info("Received location message for device id: {}", deviceId)

            val location = Location.parseFrom(message.payload as ByteArray)
            val command = ReceiveLocationCommand(
                time = location.time,
                date = location.date,
                latitude = location.latitude,
                latitudeDirection = getHemisphereChar(location.latitudeHemisphere),
                longitude = location.longitude,
                longitudeDirection = getHemisphereChar(location.longitudeHemisphere),
                altitude = location.altitude.toDouble(),
                speed = location.speed.toDouble(),
                course = location.heading.toDouble(),
                deviceId = deviceId
            )

            locationUseCase.receive(command)

        } catch (e: InvalidProtocolBufferException) {
            log.error("Failed to parse location message", e)
        }

    }

    private fun getHemisphereChar(hemisphere: Hemisphere): String = hemisphere.toString().take(1)

}
