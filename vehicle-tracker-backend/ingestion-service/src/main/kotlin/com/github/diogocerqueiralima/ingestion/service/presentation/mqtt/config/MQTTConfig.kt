package com.github.diogocerqueiralima.ingestion.service.presentation.mqtt.config

import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.core.MessageProducer
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
import org.springframework.integration.mqtt.core.MqttPahoClientFactory
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter
import org.springframework.messaging.MessageChannel
import java.io.FileInputStream
import java.security.KeyStore
import java.util.UUID
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

/**
 * Configuration class for MQTT integration.
 * This class sets up the MQTT client factory, message channel, and message producer.
 */
@Configuration
class MQTTConfig(
    @Value("\${mqtt.url}") private val mqttUrl: String,
    @Value("\${mqtt.certificate}") private val certificate: String,
    @Value("\${mqtt.certificate-password}") private val certificatePassword: String,
    @Value("\${mqtt.topic}") private val mqttTopic: String
) {

    @Bean
    fun mqttClientFactory(): MqttPahoClientFactory {

        val password = certificatePassword.toCharArray()

        // 1. Load the client certificate keystore
        val keyStore = KeyStore.getInstance("PKCS12")
        FileInputStream(certificate).use { keyStore.load(it, password) }

        // 2. Build the TLS context from it
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, password)

        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(kmf.keyManagers, null, null)

        // 3. Configure the connection options
        val options = MqttConnectOptions().apply {
            socketFactory = sslContext.socketFactory
            serverURIs = arrayOf(mqttUrl)
            isAutomaticReconnect = true
        }

        return DefaultMqttPahoClientFactory().apply { connectionOptions = options }
    }

    /**
     * The channel on which the messages will be sent.
     *
     * @return the message channel
     */
    @Bean
    fun mqttInputChannel(): MessageChannel = PublishSubscribeChannel()

    /**
     * The message producer that will connect to the MQTT broker and subscribe to the topic.
     * This is called a producer because it produces messages to the MessageChannel.
     *
     * @param mqttClientFactory the factory used to create the MQTT client
     * @param mqttInputChannel the channel on which the messages will be sent
     * @return the message producer
     */
    @Bean
    fun inbound(mqttClientFactory: MqttPahoClientFactory, mqttInputChannel: MessageChannel): MessageProducer {

        val clientId = "ingestion-${UUID.randomUUID()}"

        val converter = DefaultPahoMessageConverter().apply { setPayloadAsBytes(true) }

        return MqttPahoMessageDrivenChannelAdapter(clientId, mqttClientFactory, mqttTopic).apply {
            setConverter(converter)
            setCompletionTimeout(5000)
            setQos(0)
            setOutputChannel(mqttInputChannel)
        }
    }

}
