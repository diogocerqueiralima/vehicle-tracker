package com.github.diogocerqueiralima.gateway.service.presentation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * Binds gateway routing and rate limiting configuration from {@code application.yml}.
 *
 * @param routes      the list of routes the gateway is able to forward requests to.
 * @param rateLimit   the default and per-route rate limit settings.
 */
@ConfigurationProperties(prefix = "gateway")
public record GatewayProperties(

        List<Route> routes,
        RateLimit rateLimit

) {

    /**
     * A single downstream route.
     *
     * @param id     unique route identifier, also used as the rate limit bucket key prefix.
     * @param path   an Ant-style path pattern (e.g. {@code /assets/**}) matched against the incoming request.
     * @param uri         the base URI of the downstream service the request is forwarded to.
     * @param publicRoute whether requests matching this route are allowed without a valid JWT.
     */
    public record Route(String id, String path, String uri, boolean publicRoute) {}

    /**
     * Rate limit configuration.
     *
     * @param capacity      default max requests allowed per window.
     * @param windowSeconds default window size, in seconds.
     * @param routes        per-route overrides, keyed by route id.
     */
    public record RateLimit(int capacity, int windowSeconds, Map<String, Limit> routes) {

        public RateLimit {
            if (routes == null) {
                routes = Map.of();
            }
        }

        public Limit forRoute(String routeId) {
            return routes.getOrDefault(routeId, new Limit(capacity, windowSeconds));
        }

    }

    public record Limit(int capacity, int windowSeconds) {}

}
