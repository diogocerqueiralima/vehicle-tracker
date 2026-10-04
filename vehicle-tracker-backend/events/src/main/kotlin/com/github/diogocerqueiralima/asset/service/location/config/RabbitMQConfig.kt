package com.github.diogocerqueiralima.asset.service.location.config

import org.springframework.amqp.core.Queue
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig(private val applicationConfig: ApplicationConfig) {

    @Bean
    fun queue(): Queue = Queue(applicationConfig.locationQueueName)

    @Bean
    fun jsonMessageConverter(): JacksonJsonMessageConverter = JacksonJsonMessageConverter()

}
