package com.github.diogocerqueiralima.gateway.service.application;

import com.github.diogocerqueiralima.gateway.service.domain.RateLimitDecision;
import com.github.diogocerqueiralima.gateway.service.domain.ports.outbound.RateLimitStore;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitServiceTest {

    private final RateLimitStore rateLimitStore = mock(RateLimitStore.class);
    private final RateLimitService rateLimitService = new RateLimitService(rateLimitStore);

    @Test
    void allowsRequestWithinCapacity() {
        when(rateLimitStore.incrementAndGet(any(), anyInt())).thenReturn(Mono.just(5L));

        StepVerifier.create(rateLimitService.checkAndIncrement("asset-service", "client-1", 10, 60))
                .assertNext(decision -> {
                    assertEquals(true, decision.allowed());
                    assertEquals(5, decision.remaining());
                })
                .verifyComplete();
    }

    @Test
    void rejectsRequestExceedingCapacity() {
        when(rateLimitStore.incrementAndGet(any(), anyInt())).thenReturn(Mono.just(11L));

        StepVerifier.create(rateLimitService.checkAndIncrement("asset-service", "client-1", 10, 60))
                .assertNext(decision -> {
                    assertEquals(false, decision.allowed());
                    assertEquals(0, decision.remaining());
                    assertEquals(60, decision.retryAfterSeconds());
                })
                .verifyComplete();
    }

}
