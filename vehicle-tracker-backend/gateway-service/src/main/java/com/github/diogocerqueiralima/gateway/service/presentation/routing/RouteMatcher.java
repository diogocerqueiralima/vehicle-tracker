package com.github.diogocerqueiralima.gateway.service.presentation.routing;

import com.github.diogocerqueiralima.gateway.service.presentation.config.GatewayProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Optional;

/**
 * Resolves the {@link GatewayProperties.Route} a request path belongs to.
 * First matching route wins, so more specific patterns must be declared before broader ones.
 */
@Component
public class RouteMatcher {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<GatewayProperties.Route> routes;

    public RouteMatcher(GatewayProperties properties) {
        this.routes = properties.routes() == null ? List.of() : properties.routes();
    }

    public Optional<GatewayProperties.Route> match(String path) {
        return routes.stream()
                .filter(route -> pathMatcher.match(route.path(), path))
                .findFirst();
    }

    public List<String> publicPatterns() {
        return routes.stream()
                .filter(GatewayProperties.Route::publicRoute)
                .map(GatewayProperties.Route::path)
                .toList();
    }

}
