package com.github.diogocerqueiralima.api.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up routing in the API Gateway.
 */
@Configuration
@EnableConfigurationProperties(GatewayRoutesProperties.class)
public class RouteConfig {

    /**
     *
     * Configure the routes for the API Gateway from the services listed under {@code gateway.routes}.
     * Each route matches requests under its {@code prefix} and forwards them unchanged to the
     * service's {@code uri} — the service is expected to be mounted under that same prefix as its
     * own {@code server.servlet.context-path}, so no path rewriting is needed here.
     *
     * @param builder the {@link RouteLocatorBuilder} used to build the routes
     * @param properties the configured downstream services
     * @return the {@link RouteLocator} containing the configured routes
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder, GatewayRoutesProperties properties) {

        RouteLocatorBuilder.Builder routes = builder.routes();

        for (GatewayRoutesProperties.Route route : properties.routes()) {
            routes.route(route.id(), predicate ->
                    predicate
                            .path(route.prefix() + "/**")
                            .uri(route.uri())
            );
        }

        return routes.build();
    }

}
