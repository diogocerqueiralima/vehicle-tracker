package com.github.diogocerqueiralima.gateway.service.domain.ports.inbound;

import com.github.diogocerqueiralima.gateway.service.domain.RateLimitDecision;
import reactor.core.publisher.Mono;

/**
 * Inbound port for checking whether a request is allowed to proceed under the configured rate limits.
 */
public interface RateLimitUseCase {

    /**
     * Registers a request from {@code clientKey} against {@code routeId} and decides whether it is allowed.
     *
     * @param routeId       the route the request is targeting.
     * @param clientKey     an identifier for the caller (authenticated subject or remote address).
     * @param capacity      max requests allowed within the window.
     * @param windowSeconds size of the window, in seconds.
     * @return a {@link Mono} emitting the {@link RateLimitDecision}.
     */
    Mono<RateLimitDecision> checkAndIncrement(String routeId, String clientKey, int capacity, int windowSeconds);

}
