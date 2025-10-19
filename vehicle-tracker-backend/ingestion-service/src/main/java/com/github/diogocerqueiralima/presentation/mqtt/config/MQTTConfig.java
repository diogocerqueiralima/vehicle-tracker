package com.github.diogocerqueiralima.application.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

import java.util.UUID;

/**
 *
 * Configuration class for MQTT integration.
 * This class sets up the MQTT client factory, message channel, and message producer.
 *
 */
@Configuration
public class MQTTConfig {

    @Value("${mqtt.url}")
    private String mqttUrl;

    @Value("${mqtt.username}")
    private String mqttUsername;

    @Value("${mqtt.password}")
    private String mqttPassword;

    @Value("${mqtt.topic}")
    private String mqttTopic;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[]{mqttUrl});
        options.setUserName(mqttUsername);
        options.setPassword(mqttPassword.toCharArray());

        factory.setConnectionOptions(options);

        return factory;
    }

    /**
     *
     * The channel on which the messages will be sent.
     *
     * @return the message channel
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     *
     * The message producer that will connect to the MQTT broker and subscribe to the topic.
     * This is called a producer because it produces messages to the MessageChannel.
     *
     * @param mqttInputChannel the channel on which the messages will be sent
     * @return the message producer
     */
    @Bean
    public MessageProducer inbound(MqttPahoClientFactory mqttClientFactory, MessageChannel mqttInputChannel) {

        String clientId = "ingestion-" + UUID.randomUUID();

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId, mqttClientFactory, mqttTopic);

        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        converter.setPayloadAsBytes(true);

        adapter.setConverter(converter);
        adapter.setCompletionTimeout(5000);
        adapter.setQos(0);
        adapter.setOutputChannel(mqttInputChannel);

        return adapter;
    }

}
