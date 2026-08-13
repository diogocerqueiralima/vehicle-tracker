package com.github.diogocerqueiralima.gateway.service.domain.ports.outbound;

import reactor.core.publisher.Mono;

/**
 * Outbound port for the counter storage backing the fixed-window rate limiter.
 */
public interface RateLimitStore {

    /**
     * Atomically increments the counter for {@code key}, starting a new {@code windowSeconds} window
     * the first time the key is seen.
     *
     * @param key           the counter key, unique per route and client.
     * @param windowSeconds time-to-live of a fresh counter, in seconds.
     * @return a {@link Mono} emitting the counter value after the increment.
     */
    Mono<Long> incrementAndGet(String key, int windowSeconds);

}
