package com.github.diogocerqueiralima.gateway.service.presentation.filters;

import tools.jackson.databind.ObjectMapper;
import com.github.diogocerqueiralima.api.common.dto.ApiResponseDTO;
import com.github.diogocerqueiralima.gateway.service.domain.RateLimitDecision;
import com.github.diogocerqueiralima.gateway.service.domain.ports.inbound.RateLimitUseCase;
import com.github.diogocerqueiralima.gateway.service.presentation.config.GatewayProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * Throttles requests per client (authenticated subject when present, remote address otherwise)
 * and per route, using thresholds configured in {@code application.yml}. Runs after route
 * resolution so it can look up per-route overrides, and after Spring Security so an authenticated
 * subject is available to key the counter on.
 */
@Component
@Order(RateLimitingWebFilter.ORDER)
public class RateLimitingWebFilter implements WebFilter {

    public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 10;

    private final RateLimitUseCase rateLimitUseCase;
    private final GatewayProperties.RateLimit rateLimitProperties;
    private final ObjectMapper objectMapper;

    public RateLimitingWebFilter(
            RateLimitUseCase rateLimitUseCase, GatewayProperties properties, ObjectMapper objectMapper
    ) {
        this.rateLimitUseCase = rateLimitUseCase;
        this.rateLimitProperties = properties.rateLimit();
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        GatewayProperties.Route route =
                (GatewayProperties.Route) exchange.getAttributes().get(RouteResolutionWebFilter.ROUTE_ATTRIBUTE);

        GatewayProperties.Limit limit = rateLimitProperties.forRoute(route.id());

        return resolveClientKey(exchange)
                .flatMap(clientKey -> rateLimitUseCase.checkAndIncrement(
                        route.id(), clientKey, limit.capacity(), limit.windowSeconds()
                ))
                .flatMap(decision -> decision.allowed()
                        ? chain.filter(exchange)
                        : tooManyRequests(exchange.getResponse(), decision));
    }

    private Mono<String> resolveClientKey(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .switchIfEmpty(Mono.fromSupplier(() -> remoteAddress(exchange)));
    }

    private String remoteAddress(ServerWebExchange exchange) {
        InetSocketAddress address = exchange.getRequest().getRemoteAddress();
        return address == null || address.getAddress() == null
                ? "unknown"
                : address.getAddress().getHostAddress();
    }

    private Mono<Void> tooManyRequests(ServerHttpResponse response, RateLimitDecision decision) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().add("Retry-After", String.valueOf(decision.retryAfterSeconds()));

        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(
                    new ApiResponseDTO<>("Rate limit exceeded. Try again later.", null)
            );
        } catch (Exception e) {
            body = new byte[0];
        }

        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

}
