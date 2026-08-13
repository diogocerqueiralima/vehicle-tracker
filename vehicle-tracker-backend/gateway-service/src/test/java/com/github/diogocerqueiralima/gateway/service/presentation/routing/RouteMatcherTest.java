package com.github.diogocerqueiralima.gateway.service.presentation.routing;

import com.github.diogocerqueiralima.gateway.service.presentation.config.GatewayProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteMatcherTest {

    private final GatewayProperties properties = new GatewayProperties(
            List.of(
                    new GatewayProperties.Route("identity-service", "/identity/**", "http://identity:8080", false),
                    new GatewayProperties.Route("asset-service", "/assets/**", "http://asset:8080", true)
            ),
            new GatewayProperties.RateLimit(100, 60, null)
    );
    private final RouteMatcher routeMatcher = new RouteMatcher(properties);

    @Test
    void matchesRouteByPathPrefix() {
        var route = routeMatcher.match("/assets/123");

        assertTrue(route.isPresent());
        assertEquals("asset-service", route.get().id());
    }

    @Test
    void returnsEmptyWhenNoRouteMatches() {
        assertTrue(routeMatcher.match("/unknown/path").isEmpty());
    }

    @Test
    void collectsPublicPatternsOnly() {
        assertEquals(List.of("/assets/**"), routeMatcher.publicPatterns());
    }

    @Test
    void rateLimitFallsBackToDefaultForUnconfiguredRoute() {
        GatewayProperties.Limit limit = properties.rateLimit().forRoute("identity-service");

        assertEquals(100, limit.capacity());
        assertEquals(60, limit.windowSeconds());
        assertFalse(routeMatcher.publicPatterns().contains("/identity/**"));
    }

}
