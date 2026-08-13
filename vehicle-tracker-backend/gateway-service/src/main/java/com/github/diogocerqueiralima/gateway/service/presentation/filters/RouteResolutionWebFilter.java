package com.github.diogocerqueiralima.gateway.service.presentation.filters;

import tools.jackson.databind.ObjectMapper;
import com.github.diogocerqueiralima.api.common.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.gateway.service.presentation.config.GatewayProperties;
import com.github.diogocerqueiralima.gateway.service.presentation.routing.RouteMatcher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Resolves the downstream route for the incoming request path and stores it as an exchange
 * attribute for downstream filters. Requests matching no configured route are rejected with 404
 * before reaching authentication or rate limiting, so unknown paths never leak to backend services.
 */
@Component
@Order(RouteResolutionWebFilter.ORDER)
public class RouteResolutionWebFilter implements WebFilter {

    public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 20;
    public static final String ROUTE_ATTRIBUTE = "gateway.route";

    private final RouteMatcher routeMatcher;
    private final ObjectMapper objectMapper;

    public RouteResolutionWebFilter(RouteMatcher routeMatcher, ObjectMapper objectMapper) {
        this.routeMatcher = routeMatcher;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getPath().value();
        var route = routeMatcher.match(path);

        if (route.isEmpty()) {
            return writeError(exchange.getResponse(), HttpStatus.NOT_FOUND, "No route matches this path.");
        }

        exchange.getAttributes().put(ROUTE_ATTRIBUTE, route.get());
        return chain.filter(exchange);
    }

    private Mono<Void> writeError(ServerHttpResponse response, HttpStatus status, String message) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(new ApiResponseDTO<>(message, null));
        } catch (Exception e) {
            body = new byte[0];
        }

        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

}
