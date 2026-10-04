package com.github.diogocerqueiralima.api.gateway.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for setting up routing in the API Gateway.
 */
@Configuration
@EnableConfigurationProperties(GatewayRoutesProperties::class)
class RouteConfig {

    /**
     * Configure the routes for the API Gateway from the services listed under `gateway.routes`.
     * Each route matches requests under its `prefix` and forwards them unchanged to the
     * service's `uri` — the service is expected to be mounted under that same prefix as its
     * own `server.servlet.context-path`, so no path rewriting is needed here.
     *
     * @param builder the [RouteLocatorBuilder] used to build the routes
     * @param properties the configured downstream services
     * @return the [RouteLocator] containing the configured routes
     */
    @Bean
    fun routeLocator(builder: RouteLocatorBuilder, properties: GatewayRoutesProperties): RouteLocator {

        val routes = builder.routes()

        properties.routes.forEach { route ->
            routes.route(route.id) { predicate ->
                predicate
                    .path("${route.prefix}/**")
                    .uri(route.uri)
            }
        }

        return routes.build()
    }

}
