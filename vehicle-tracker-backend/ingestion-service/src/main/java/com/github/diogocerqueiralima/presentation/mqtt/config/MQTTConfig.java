package com.github.diogocerqueiralima.presentation.mqtt.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.UUID;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

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

    @Value("${mqtt.certificate}")
    private String certificate;

    @Value("${mqtt.certificate-password}")
    private String certificatePassword;

    @Value("${mqtt.topic}")
    private String mqttTopic;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() throws Exception {

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(certificate)) {
            keyStore.load(fis, certificatePassword.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, certificatePassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        options.setSocketFactory(sslSocketFactory);
        options.setServerURIs(new String[]{mqttUrl});
        options.setAutomaticReconnect(true);

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
        return new PublishSubscribeChannel();
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
