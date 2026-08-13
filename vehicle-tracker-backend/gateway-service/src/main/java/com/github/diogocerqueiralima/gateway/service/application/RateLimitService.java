package com.github.diogocerqueiralima.gateway.service.application;

import com.github.diogocerqueiralima.gateway.service.domain.RateLimitDecision;
import com.github.diogocerqueiralima.gateway.service.domain.ports.inbound.RateLimitUseCase;
import com.github.diogocerqueiralima.gateway.service.domain.ports.outbound.RateLimitStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RateLimitService implements RateLimitUseCase {

    private final RateLimitStore rateLimitStore;

    public RateLimitService(RateLimitStore rateLimitStore) {
        this.rateLimitStore = rateLimitStore;
    }

    @Override
    public Mono<RateLimitDecision> checkAndIncrement(String routeId, String clientKey, int capacity, int windowSeconds) {

        // 1. Counter is scoped per route and per client so limits do not leak across routes/clients.
        String key = "rate-limit:%s:%s".formatted(routeId, clientKey);

        // 2. Atomically bump counter for current window and compare against configured capacity.
        return rateLimitStore.incrementAndGet(key, windowSeconds)
                .map(count -> count <= capacity
                        ? new RateLimitDecision(true, Math.max(0, capacity - count), 0)
                        : new RateLimitDecision(false, 0, windowSeconds));
    }

}
