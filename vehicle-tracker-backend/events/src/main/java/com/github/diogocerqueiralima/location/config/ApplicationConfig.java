package com.github.diogocerqueiralima.location.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Value("${queues.location.name}")
    private String locationQueueName;

    public String getLocationQueueName() {
        return locationQueueName;
    }

}
