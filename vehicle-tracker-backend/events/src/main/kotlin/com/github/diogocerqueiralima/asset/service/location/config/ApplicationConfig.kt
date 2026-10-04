package com.github.diogocerqueiralima.asset.service.location.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig(@Value("\${queues.location.name}") val locationQueueName: String)
